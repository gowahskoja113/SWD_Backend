package com.swd.evdms.controller;

import com.swd.evdms.dto.request.PurchaseOrderCreateRequest;
import com.swd.evdms.dto.response.PurchaseOrderResponse;
import com.swd.evdms.service.PurchaseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/po")
public class PurchaseOrderController {
    private final PurchaseOrderService service;

    public PurchaseOrderController(PurchaseOrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@RequestBody PurchaseOrderCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}

