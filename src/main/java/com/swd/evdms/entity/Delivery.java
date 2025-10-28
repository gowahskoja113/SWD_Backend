package com.swd.evdms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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
    @Column(nullable = false)
    private LocalDateTime deliveryDate;
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, length = 50)
    private String status; //  "Pending", "Delivered", "Cancelled"
}
