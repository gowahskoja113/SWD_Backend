package com.swd.evdms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private ElectricVehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "vehicle_unit_id")
    private VehicleUnit vehicleUnit;
    @Column(nullable = false)
    private LocalDateTime deliveryDate;
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, length = 50)
    private String status; //  "Pending", "Delivered", "Cancelled"

    private String customerName;
    private BigDecimal priceBefore;
    private BigDecimal discountApplied;
    private BigDecimal priceAfter;
    private BigDecimal deposit;

    public VehicleUnit getVehicleUnit() { return vehicleUnit; }
    public void setVehicleUnit(VehicleUnit vehicleUnit) { this.vehicleUnit = vehicleUnit; }
}
