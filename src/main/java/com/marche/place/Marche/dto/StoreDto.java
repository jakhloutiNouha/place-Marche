package com.marche.place.Marche.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class StoreDto {
    private Long id;
    private String title;
    private String description;
    private UserDto vendor;
    private LocalDateTime createdAt;
}
