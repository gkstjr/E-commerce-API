package hhplus.ecommerce.application.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.support.exception.BusinessException;
import hhplus.ecommerce.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageConverter {

    private final ObjectMapper objectMapper;
    /**
     * JSON 문자열을 DTO 객체로 변환
     */
    public <T> T deserialize(String json, Class<T> targetType) {
        try {
            return objectMapper.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.ORDER_JSON_PARSE_ERROR);
        }
    }

    /**
     * DTO 객체를 JSON 문자열로 변환
     */
    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.ORDER_JSON_PARSE_ERROR);
        }
    }
}
