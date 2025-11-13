package com.swd.evdms.repository;

import com.swd.evdms.entity.VehicleUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleUnitRepository extends JpaRepository<VehicleUnit, Long> {
    List<VehicleUnit> findByStatus(String status);
    Optional<VehicleUnit> findByVin(String vin);
}
