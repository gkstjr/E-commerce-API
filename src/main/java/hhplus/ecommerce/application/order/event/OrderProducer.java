package hhplus.ecommerce.application.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import hhplus.ecommerce.support.exception.BusinessException;
import hhplus.ecommerce.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OrderProducer {

    private final KafkaTemplate<String , String> kafkaTemplate;
    private final MessageConverter messageConverter;
    @Transactional
    public void sendMessage(OrderEvent event) {
        try {
            String message = messageConverter.serialize(event);
            kafkaTemplate.send("order-create.v1",message);
        }catch (Exception e) {
            throw new BusinessException(ErrorCode.Kafka_NETWORT_FAILED);
        }
    }
}
