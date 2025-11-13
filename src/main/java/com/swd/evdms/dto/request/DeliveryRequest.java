package com.swd.evdms.dto.request;

import lombok.Data;

@Data
public class DeliveryRequest {
    private Long orderId;
    private Long vehicleId;
    private Long vehicleUnitId;
    private String status;
    private String customerName;
    private java.math.BigDecimal priceBefore;
    private String voucherCode;
    private java.math.BigDecimal deposit;
}
