package com.swd.evdms.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OrderResponse {

    private Long id;
    private Integer userId;
    private String username;
    private Long vehicleId;
    private String vehicleModel;
    private String status;
    private String customerInfo;
    private String brand;
    private BigDecimal price;


}
