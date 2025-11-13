package com.swd.evdms.repository;

import com.swd.evdms.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);
    java.util.List<Voucher> findByActiveTrue();
}
