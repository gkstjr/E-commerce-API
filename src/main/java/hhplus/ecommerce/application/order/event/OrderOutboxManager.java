package hhplus.ecommerce.application.order.event;

import hhplus.ecommerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderOutboxManager {
    private final MessageConverter converter;
    private final OrderRepository orderRepository;

    @Transactional
    public void saveOutbox(OrderEvent event) {
        String payload = converter.serialize(event);

        orderRepository.saveOutbox(OrderOutbox.create(payload , event.getEventId()));
    }
}
