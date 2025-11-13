package com.swd.evdms.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "manufacturer_order")
public class ManufacturerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNo;

    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT, SUBMITTED, CONFIRMED, CANCELLED

    private LocalDateTime etaAtDealer;
    private String note;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private ElectricVehicle model;

    private Integer quantity;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void touch() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getEtaAtDealer() { return etaAtDealer; }
    public void setEtaAtDealer(LocalDateTime etaAtDealer) { this.etaAtDealer = etaAtDealer; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public ElectricVehicle getModel() { return model; }
    public void setModel(ElectricVehicle model) { this.model = model; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
