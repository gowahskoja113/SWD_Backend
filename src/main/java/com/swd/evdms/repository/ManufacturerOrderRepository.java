package com.swd.evdms.repository;

import com.swd.evdms.entity.ManufacturerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerOrderRepository extends JpaRepository<ManufacturerOrder, Long> {
    boolean existsByOrderNo(String orderNo);
}

