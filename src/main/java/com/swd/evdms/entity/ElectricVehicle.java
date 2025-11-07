package com.swd.evdms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "electric_vehicle")
public class ElectricVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String model;
    private BigDecimal cost;
    private BigDecimal price;
    private String status;
    private String batteryCapacity;
    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleStock> vehicleStocks = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "discount_id")
    private DiscountPolicy discountPolicy;

}
