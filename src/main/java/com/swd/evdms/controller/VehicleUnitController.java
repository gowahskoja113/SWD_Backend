package com.swd.evdms.controller;

import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.entity.VehicleUnit;
import com.swd.evdms.repository.ElectricVehicleRepository;
import com.swd.evdms.repository.VehicleUnitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-units")
public class VehicleUnitController {
    private final VehicleUnitRepository unitRepo;
    private final ElectricVehicleRepository modelRepo;

    public VehicleUnitController(VehicleUnitRepository unitRepo, ElectricVehicleRepository modelRepo) {
        this.unitRepo = unitRepo;
        this.modelRepo = modelRepo;
    }

    @GetMapping
    public ResponseEntity<List<VehicleUnit>> list(@RequestParam(required = false) String status) {
        List<VehicleUnit> out = (status == null || status.isBlank()) ? unitRepo.findAll() : unitRepo.findByStatus(status);
        return ResponseEntity.ok(out);
    }

    @PostMapping
    public ResponseEntity<VehicleUnit> create(@RequestParam Long modelId) {
        ElectricVehicle model = modelRepo.findById(modelId).orElseThrow(() -> new RuntimeException("Model not found"));
        VehicleUnit u = new VehicleUnit();
        u.setModel(model);
        u.setStatus("ON_ORDER");
        return ResponseEntity.ok(unitRepo.save(u));
    }

    @PatchMapping("/{id}/arrive")
    public ResponseEntity<VehicleUnit> markArrived(@PathVariable Long id) {
        VehicleUnit u = unitRepo.findById(id).orElseThrow(() -> new RuntimeException("Unit not found"));
        if ("DELIVERED".equalsIgnoreCase(u.getStatus())) {
            throw new RuntimeException("Cannot mark arrived: vehicle already delivered");
        }
        u.setStatus("AT_DEALER");
        u.setArrivedAt(LocalDateTime.now());
        return ResponseEntity.ok(unitRepo.save(u));
    }

    @PatchMapping("/{id}/vin")
    public ResponseEntity<VehicleUnit> setVin(@PathVariable Long id, @RequestParam String vin) {
        VehicleUnit u = unitRepo.findById(id).orElseThrow(() -> new RuntimeException("Unit not found"));
        if ("DELIVERED".equalsIgnoreCase(u.getStatus())) {
            throw new RuntimeException("Cannot set VIN: vehicle already delivered");
        }
        if (vin == null || vin.isBlank()) throw new RuntimeException("VIN required");
        var existed = unitRepo.findByVin(vin).orElse(null);
        if (existed != null && !existed.getId().equals(id)) {
            throw new RuntimeException("VIN already exists");
        }
        u.setVin(vin);
        return ResponseEntity.ok(unitRepo.save(u));
    }
}
