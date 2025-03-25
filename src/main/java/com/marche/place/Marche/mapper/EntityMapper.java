package com.marche.place.Marche.mapper;

import com.marche.place.Marche.dto.*;
import com.marche.place.Marche.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EntityMapper {
    EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

    CategoryDto toCategoryDTO(Category category);
    Category toCategory(CategoryDto categoryDTO);

    ProductDto toProductDTO(Product product);
    Product toProduct(ProductDto productDTO);

    UserDto toUserDTO(User user);
    User toUser(UserDto userDTO);

    OrderItemDto toOrderItemDTO(OrderItem orderItem);
    OrderItem toOrderItem(OrderItemDto orderItemDTO);

    OrderDto toOrderDTO(Order order);
    Order toOrder(OrderDto orderDTO);

    StoreDto toShopDTO(Store store);
    Store toShop(StoreDto storeDto);

    PromotionDto toPromotionDTO(Promotion promotion);
    Promotion toPromotion(PromotionDto promotionDTO);

    PaymentDto toPaymentDTO(Payment payment);
    Payment toPayment(PaymentDto paymentDTO);
}