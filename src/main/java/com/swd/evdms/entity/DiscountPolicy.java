package com.swd.evdms.entity;


import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "discount_policy")
public class DiscountPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 30)
    private String discountType;  // e.g. "PERCENTAGE" or "FIXED"

    @Column(nullable = false)
    private double discountValue;

    @Column(nullable = false)
    private double minOrderValue;

    @Column(nullable = false)
    private int maxUsage;

    @Column(nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    @OneToMany(mappedBy = "discountPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ElectricVehicle> vehicles;
}
