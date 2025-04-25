// VendorRequestDto.java
package com.marche.place.Marche.dto;

import com.marche.place.Marche.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRequestDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String storeName;
    private String storeDescription;
    private String storeAddress;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String rejectionReason;
}