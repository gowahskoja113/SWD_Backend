package com.swd.evdms.dto.request;

public class PurchaseOrderCreateRequest {
    public String orderNo;
    public String etaAtDealer; // ISO datetime string
    public String note;
    public Long modelId;
    public Integer quantity;
}

