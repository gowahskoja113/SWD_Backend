package com.swd.evdms.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class DeliveryResponse {
    private Long id;
    private Long orderId;
    private Long vehicleId;
    private String vehicleName;
    private LocalDateTime deliveryDate;
    private String status;
    private Long vehicleUnitId;
    private String staffName;
    private String customerName;
    private java.math.BigDecimal priceBefore;
    private java.math.BigDecimal discountApplied;
    private java.math.BigDecimal priceAfter;
    private java.math.BigDecimal deposit;
}
