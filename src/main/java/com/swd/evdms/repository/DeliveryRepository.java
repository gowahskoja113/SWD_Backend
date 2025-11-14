package com.swd.evdms.repository;

import com.swd.evdms.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {
    boolean existsByOrder_Id(Long orderId);
    java.util.Optional<Delivery> findByOrder_Id(Long orderId);
    java.util.List<Delivery> findByOrder_User_UserId(Integer userId);
}
