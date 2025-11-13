package com.swd.evdms.controller;

import com.swd.evdms.entity.Voucher;
import com.swd.evdms.repository.VoucherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    private final VoucherRepository repo;
    public VoucherController(VoucherRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Voucher>> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        List<Voucher> data = includeInactive ? repo.findAll() : repo.findByActiveTrue();
        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<Voucher> create(@RequestBody Voucher v) {
        if (v.getCode() == null || v.getCode().isBlank()) throw new RuntimeException("Code required");
        if (v.getType() == null || v.getType().isBlank()) throw new RuntimeException("Type required");
        v.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(v));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Voucher> setActive(@PathVariable Long id, @RequestParam boolean active) {
        Voucher v = repo.findById(id).orElseThrow(() -> new RuntimeException("Voucher not found"));
        v.setActive(active);
        return ResponseEntity.ok(repo.save(v));
    }
}
