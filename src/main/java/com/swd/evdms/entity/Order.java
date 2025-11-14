package com.swd.evdms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private ElectricVehicle vehicle;

    private BigDecimal price;
    private BigDecimal priceAfter;

    @Column(precision = 19, scale = 2)
    private BigDecimal discountApplied;

    private String voucherCode;

    private LocalDate deliveryDate;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void touch() { this.updatedAt = LocalDateTime.now(); }

    @Column(name = "customer_info", length = 255)
    private String customerInfo;

    private String brand;

    private String status;
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Delivery delivery;


}
