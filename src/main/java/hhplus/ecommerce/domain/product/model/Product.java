package hhplus.ecommerce.domain.product.model;

import hhplus.ecommerce.common.BaseEntity;
import hhplus.ecommerce.domain.order.model.OrderProduct;
import hhplus.ecommerce.domain.product.stock.ProductStock;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private long price;
    @OneToOne(mappedBy = "product" , cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductStock productStock;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL , orphanRemoval = true)
    private List<OrderProduct> orderProductList = new ArrayList<>();

    public void setProductStock(ProductStock productStock) {
        this.productStock = productStock;
        productStock.addProduct(this);
    }
}
