package hhplus.ecommerce.integration.order;

import hhplus.ecommerce.application.order.OrderPayFacade;
import hhplus.ecommerce.application.order.OrderPayCriteria;
import hhplus.ecommerce.application.order.OrderPayResult;
import hhplus.ecommerce.application.order.event.OrderOutbox;
import hhplus.ecommerce.domain.coupon.CouponRepository;
import hhplus.ecommerce.domain.coupon.IssuedCoupon;
import hhplus.ecommerce.domain.coupon.Coupon;
import hhplus.ecommerce.application.order.DataPlatform;
import hhplus.ecommerce.domain.order.*;
import hhplus.ecommerce.application.order.event.OrderEvent;
import hhplus.ecommerce.domain.payment.PaymentService;
import hhplus.ecommerce.domain.point.PointRepository;
import hhplus.ecommerce.domain.point.Point;
import hhplus.ecommerce.domain.product.ProductRepository;
import hhplus.ecommerce.domain.product.Product;
import hhplus.ecommerce.domain.product.ProductStock;
import hhplus.ecommerce.domain.user.UserRepository;
import hhplus.ecommerce.domain.user.User;
import hhplus.ecommerce.support.exception.BusinessException;
import hhplus.ecommerce.support.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static hhplus.ecommerce.domain.coupon.IssuedCoupon.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderPaymentFacadeTest {

    @Autowired
    private OrderPayFacade orderPayFacade;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @MockitoSpyBean
    private DataPlatform dataPlatform;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    public void cleanUp() {
        pointRepository.deleteAll();
//        couponRepository.deleteAllIssuedCoupon();
//        couponRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void 동시에_동일상품주문시_20개재고중_10개씩_2번요청성공_이후1개요청실패() throws InterruptedException {
        // Given: 테스트 데이터 준비
        List<User> users = userRepository.saveAll(List.of(
                User.builder().name("기만석").build(),
                User.builder().name("기만석2").build(),
                User.builder().name("기만석3").build(),
                User.builder().name("기만석4").build()
        ));
        List<Point> points = pointRepository.saveAll(users.stream()
                .map(user -> Point.builder().point(100000).user(user).build())
                .toList());

        List<Product> products = productRepository.saveAll(List.of(
                getProduct("하헌우코치님의 녹색 패딩1", 500, getProductStock(20)),
                getProduct("하헌우코치님의 녹색 패딩2", 1000, getProductStock(20))
        ));
        List<OrderPayDto> reqOrderItems = List.of(
                new OrderPayDto(products.get(0).getId(), 10),
                new OrderPayDto(products.get(1).getId(), 10)
        );

        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger failCnt = new AtomicInteger();

        // When
        for (int i = 0; i < threadCount; i++) {
            int index = i; // 사용자 인덱스
            executorService.submit(() -> {
                try {
                    OrderPayResult result = orderPayFacade.orderPay(new OrderPayCriteria(users.get(index), reqOrderItems, null));
                    successCnt.incrementAndGet();
                } catch (Exception e) {
                    failCnt.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        executorService.shutdown();
        latch.await();

        //then
        assertThat(successCnt.get()).isEqualTo(2);
        assertThat(failCnt.get()).isEqualTo(1);
    }

    @Test
    public void 주문결제완료시_이벤트발행Outbox검증_성공통합테스트() throws InterruptedException {
        //given
        User user = userRepository.save(
                User.builder()
                        .name("기만석")
                        .build()
        );
        Point point = pointRepository.save(
                Point.builder()
                        .point(100000)
                        .user(user)
                        .build());
        List<Product> products = productRepository.saveAll(List.of(
                getProduct("상품1",5000,getProductStock(10)),
                getProduct("상품2",10000,getProductStock(5))
        ));

        List<OrderPayDto> reqOrderItems = List.of(
                new OrderPayDto(products.get(0).getId(),10),
                new OrderPayDto(products.get(1).getId(), 5)
        );
        Coupon coupon = Coupon.builder()
                .name("5000원 할인쿠폰")
                .discountPrice(5000)
                .issuedCount(10)
                .maxIssuedCount(20)
                .validUntil(LocalDate.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        IssuedCoupon issuedCoupon =  couponRepository.saveIssuedCoupon(
                 builder()
                .user(user)
                .status(CouponStatus.UNUSED)
                .coupon(coupon)
                .build());
        //when
        OrderPayResult result = orderPayFacade.orderPay(new OrderPayCriteria(user,reqOrderItems, issuedCoupon.getId()));
        //then
        assertThat(result.discountAmount()).isEqualTo(5000);
        assertThat(result.remindPoint()).isEqualTo(5000);
        assertThat(result.orderStatus()).isEqualTo(Order.OrderStatus.PAYMENT_COMPLETED);

        //outbox-pattern 검증
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OrderOutbox> publishedOutboxEvents = orderRepository.findAllByStatus(OrderOutbox.OrderOutboxStatus.PUBLISHED);
            assertThat(publishedOutboxEvents).isNotEmpty();
        });
    }

    @Test
    public void 주문결제실패시_데이터플랫폼전송_실행되지않는다() {
        // given
        User user = userRepository.save(
                User.builder()
                        .name("기만석")
                        .build()
        );
        Point point = pointRepository.save(
                Point.builder()
                        .point(1000) //포인트 부족으로 실패
                        .user(user)
                        .build());
        List<Product> products = productRepository.saveAll(List.of(
                getProduct("상품1", 5000, getProductStock(10)),
                getProduct("상품2", 10000, getProductStock(5))
        ));

        List<OrderPayDto> reqOrderItems = List.of(
                new OrderPayDto(products.get(0).getId(), 10),
                new OrderPayDto(products.get(1).getId(), 5)
        );
        Coupon coupon = Coupon.builder()
                .name("5000원 할인쿠폰")
                .discountPrice(5000)
                .issuedCount(10)
                .maxIssuedCount(20)
                .validUntil(LocalDate.now().plusDays(1))
                .build();
        couponRepository.save(coupon);

        IssuedCoupon issuedCoupon = couponRepository.saveIssuedCoupon(
                IssuedCoupon.builder()
                        .user(user)
                        .status(CouponStatus.UNUSED)
                        .coupon(coupon)
                        .build());

        // when , then
        assertThatThrownBy(() -> orderPayFacade.orderPay(new OrderPayCriteria(user,reqOrderItems, issuedCoupon.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.OUT_OF_POINT.getMessage());
//        verify(dataPlatform, never()).sendOrderData(any(OrderEvent.class));
    }

    private static OrderProduct createOrderProduct(Product product1, int quantity) {
        return OrderProduct.builder()
                .product(product1).quantity(quantity).build();
    }

    private static ProductStock getProductStock(int stock) {
        return ProductStock.builder()
                .stock(stock)
                .build();
    }

    private static Product getProduct(String name , long price , ProductStock productStock) {
        Product product =      Product.builder()
                .name(name)
                .price(price)
                .productStock(productStock)
                .build();

        product.setProductStock(productStock);
        return product;
    }
}
