package com.swd.evdms.dto.request;

import lombok.Data;

@Data
public class DeliveryRequest {
    private Long orderId;
    private Long vehicleId;
    private String status;
}
