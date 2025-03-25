package com.marche.place.Marche.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {
    private Long id;
    private String title;  // Modification : Remplace "name" par "title"
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    private Long categoryId;  // Utiliser uniquement l'ID de la catégorie
    private Long storeId;  // Utiliser uniquement l'ID de la boutique
}
