package hhplus.ecommerce.application.order.event;

import hhplus.ecommerce.application.order.DataPlatform;
import hhplus.ecommerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final DataPlatform dataPlatform;
    private final OrderRepository orderRepository;
    private final MessageConverter converter;
    @KafkaListener(topics = "order-create.v1", groupId = "order-outbox-validator-group")
    @Transactional
    public void validateOrderOutbox(String message) {
            OrderEvent event = converter.deserialize(message,OrderEvent.class);

            orderRepository.findByEventId(event.getEventId()).ifPresent(OrderOutbox::checkPublished);
    }

    @KafkaListener(topics = "order-create.v1", groupId = "order-data-platform-group")
    @Transactional
    public void sendDataPlatform(String message) {
            OrderEvent event = converter.deserialize(message, OrderEvent.class);

            dataPlatform.sendOrderData(event);
    }
}
