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
}
