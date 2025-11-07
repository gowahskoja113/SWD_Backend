package com.swd.evdms.controller;

import com.swd.evdms.dto.request.DeliveryRequest;
import com.swd.evdms.dto.request.DeliveryStatusUpdateRequest;
import com.swd.evdms.dto.response.DeliveryResponse;
import com.swd.evdms.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public DeliveryResponse create(@RequestBody DeliveryRequest req) {
        return deliveryService.create(req);
    }

    @PatchMapping("/{id}/status")
    public DeliveryResponse updateStatus(@PathVariable Long id,
                                         @RequestBody DeliveryStatusUpdateRequest req) {
        return deliveryService.updateStatus(id, req.getStatus());
    }

    @GetMapping
    public List<DeliveryResponse> list() {
        return deliveryService.listAll();
    }
}
