package com.swd.evdms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_unit")
public class VehicleUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "model_id")
    private ElectricVehicle model;

    @Column
    private String vin;

    @Column(nullable = false)
    private String status = "ON_ORDER"; // ON_ORDER, AT_DEALER, DELIVERED

    private LocalDateTime arrivedAt;
    private LocalDateTime deliveredAt;
    @ManyToOne
    @JoinColumn(name = "po_id")
    private ManufacturerOrder purchaseOrder;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void touch() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ElectricVehicle getModel() { return model; }
    public void setModel(ElectricVehicle model) { this.model = model; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public void setArrivedAt(LocalDateTime arrivedAt) { this.arrivedAt = arrivedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public ManufacturerOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(ManufacturerOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
}
