package com.swd.evdms.repository;

import com.swd.evdms.entity.ElectricVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ElectricVehicleRepository extends JpaRepository<ElectricVehicle, Integer> {
}
