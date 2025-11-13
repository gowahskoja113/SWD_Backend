package com.swd.evdms.controller;

import com.swd.evdms.dto.response.VehicleResponse;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.repository.ElectricVehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final ElectricVehicleRepository vehicleRepository;

    public VehicleController(ElectricVehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> list() {
        List<VehicleResponse> out = vehicleRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    private VehicleResponse toDto(ElectricVehicle v) {
        String brandName = v.getBrand() != null ? v.getBrand().getName() : null;
        return new VehicleResponse(v.getId(), v.getModel(), brandName, v.getStatus(), v.getBatteryCapacity());
    }
}

