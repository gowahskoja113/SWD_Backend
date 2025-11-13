package com.swd.evdms.controller;

import com.swd.evdms.entity.Brand;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.repository.ElectricVehicleRepository;
import com.swd.evdms.repository.BrandRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/models")
public class ModelController {
    private final ElectricVehicleRepository repo;
    private final BrandRepository brandRepo;
    public ModelController(ElectricVehicleRepository repo, BrandRepository brandRepo) { this.repo = repo; this.brandRepo = brandRepo; }

    @GetMapping
    public ResponseEntity<java.util.List<ElectricVehicle>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @PostMapping
    public ResponseEntity<ElectricVehicle> create(@RequestParam String model,
                                                  @RequestParam(defaultValue = "0") BigDecimal cost,
                                                  @RequestParam(defaultValue = "0") BigDecimal price,
                                                  @RequestParam(defaultValue = "AVAILABLE") String status,
                                                  @RequestParam(defaultValue = "DemoBrand") String brand) {
        ElectricVehicle ev = new ElectricVehicle();
        ev.setModel(model);
        ev.setCost(cost);
        ev.setPrice(price);
        ev.setStatus(status);
        Brand b = brandRepo.findByName(brand).orElseGet(() -> {
            Brand nb = new Brand();
            nb.setName(brand);
            return brandRepo.save(nb);
        });
        ev.setBrand(b);
        return ResponseEntity.ok(repo.save(ev));
    }
}
