package com.swd.evdms.dto.request;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OrderRequest {
    private Long vehicleId;
    private String customerInfo;
    private String brand;
    private BigDecimal price;
    private String voucherCode;
    private LocalDate deliveryDate;
}
