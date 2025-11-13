package com.swd.evdms.service;

import com.swd.evdms.dto.request.PurchaseOrderCreateRequest;
import com.swd.evdms.dto.response.PurchaseOrderResponse;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.entity.ManufacturerOrder;
import com.swd.evdms.entity.VehicleUnit;
import com.swd.evdms.repository.ElectricVehicleRepository;
import com.swd.evdms.repository.ManufacturerOrderRepository;
import com.swd.evdms.repository.VehicleUnitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {
    private final ManufacturerOrderRepository poRepo;
    private final ElectricVehicleRepository modelRepo;
    private final VehicleUnitRepository unitRepo;

    public PurchaseOrderService(ManufacturerOrderRepository poRepo, ElectricVehicleRepository modelRepo, VehicleUnitRepository unitRepo) {
        this.poRepo = poRepo;
        this.modelRepo = modelRepo;
        this.unitRepo = unitRepo;
    }

    public PurchaseOrderResponse create(PurchaseOrderCreateRequest req) {
        if (poRepo.existsByOrderNo(req.orderNo)) throw new RuntimeException("PO exists");
        ManufacturerOrder po = new ManufacturerOrder();
        po.setOrderNo(req.orderNo);
        if (req.etaAtDealer != null && !req.etaAtDealer.isBlank()) {
            po.setEtaAtDealer(parseEta(req.etaAtDealer));
        }
        po.setNote(req.note);
        po.setStatus("DRAFT");
        ManufacturerOrder saved = poRepo.save(po);

        if (req.modelId != null && req.quantity != null && req.quantity > 0) {
            ElectricVehicle model = modelRepo.findById(req.modelId).orElseThrow(() -> new RuntimeException("Model not found"));
            for (int i = 0; i < req.quantity; i++) {
                VehicleUnit u = new VehicleUnit();
                u.setModel(model);
                u.setStatus("ON_ORDER");
                u.setPurchaseOrder(saved);
                unitRepo.save(u);
            }
        }

        return toDto(saved);
    }

    public List<PurchaseOrderResponse> list() {
        return poRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public PurchaseOrderResponse updateStatus(Long id, String status) {
        ManufacturerOrder po = poRepo.findById(id).orElseThrow(() -> new RuntimeException("PO not found"));
        po.setStatus(status);
        return toDto(poRepo.save(po));
    }

    private PurchaseOrderResponse toDto(ManufacturerOrder po) {
        PurchaseOrderResponse r = new PurchaseOrderResponse();
        r.id = po.getId();
        r.orderNo = po.getOrderNo();
        r.status = po.getStatus();
        r.etaAtDealer = po.getEtaAtDealer();
        r.note = po.getNote();
        r.createdAt = po.getCreatedAt();
        r.updatedAt = po.getUpdatedAt();
        return r;
    }

    private LocalDateTime parseEta(String input) {
        try {
            // Accept plain LocalDateTime: 2025-11-13T10:00:00
            return LocalDateTime.parse(input);
        } catch (Exception ignore) {}
        try {
            // Accept ISO offset like 2025-11-13T00:00:00.000Z or with timezone offset
            return java.time.OffsetDateTime.parse(input).toLocalDateTime();
        } catch (Exception ignore) {}
        try {
            // Accept date only: 2025-11-13
            return java.time.LocalDate.parse(input).atStartOfDay();
        } catch (Exception ignore) {}
        try {
            // Accept Instant string
            return java.time.Instant.parse(input).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ignore) {}
        throw new RuntimeException("Invalid ETA datetime format: " + input);
    }
}
