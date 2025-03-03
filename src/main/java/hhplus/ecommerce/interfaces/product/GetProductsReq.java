package hhplus.ecommerce.interfaces.product;

import hhplus.ecommerce.domain.product.GetProductsByFilterCommand;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record GetProductsReq(
        String productName,
        Long minPrice,
        Long maxPrice,
        int page,
        int size
) {
    public GetProductsReq {
        if (page < 0) page = 0;
        if (size <= 0) size = 100;
    }

    public GetProductsByFilterCommand toCommand() {
        return new GetProductsByFilterCommand(productName, minPrice, maxPrice);
    }

    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
