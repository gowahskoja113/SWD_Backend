package com.swd.evdms.repository;

import com.swd.evdms.entity.ElectricVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ElectricVehicleRepository extends JpaRepository<ElectricVehicle, Long> {


    // Nếu CHƯA có field available, dùng tạm xe bất kỳ:
    Optional<ElectricVehicle> findFirstByOrderByIdAsc();
}
