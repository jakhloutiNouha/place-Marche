package com.marche.place.Marche.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.marche.place.Marche.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String imageUrl;
    private Integer stock;
    private Long categoryId;
    private Long storeId;
    private Long vendorId;

    public ProductDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.discountedPrice = product.getDiscountedPrice();
        this.imageUrl = product.getImageUrl();
        this.stock = product.getStock();
        this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        this.storeId = product.getStoreId();
        this.vendorId = product.getVendor() != null ? product.getVendor().getId() : null;
    }
}
