package hhplus.ecommerce.domain.product;

import hhplus.ecommerce.domain.order.IOrderProductRepository;
import hhplus.ecommerce.domain.product.dto.GetProductsByFilterCommand;
import hhplus.ecommerce.domain.product.dto.PopularProductDto;
import hhplus.ecommerce.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final IProductRepository iProductRepository;
    private final IOrderProductRepository iOrderProductRepository;

    public ProductService(IProductRepository iProductRepository, IOrderProductRepository iOrderProductRepository) {
        this.iProductRepository = iProductRepository;
        this.iOrderProductRepository = iOrderProductRepository;
    }

    public Page<Product> getProductsByFilter(GetProductsByFilterCommand command , Pageable pageable) {
        return  iProductRepository.findByProductWithStockByFilter(command.productName() , command.minPrice() , command.maxPrice(),pageable);
    }

    public List<PopularProductDto> getPopularProduct3Days() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(3);
        Pageable three = PageRequest.of(0,3);

        return iOrderProductRepository.findPoplarProductBeforeDays(startDate,endDate,three);
    }
}
