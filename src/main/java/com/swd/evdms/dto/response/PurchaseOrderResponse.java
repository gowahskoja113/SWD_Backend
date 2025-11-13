package com.swd.evdms.dto.response;

import java.time.LocalDateTime;

public class PurchaseOrderResponse {
    public Long id;
    public String orderNo;
    public String status;
    public LocalDateTime etaAtDealer;
    public String note;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

