package com.swd.evdms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(name = "voucher_type", nullable = false, length = 20)
    private String type; // FLAT or PERCENT
    private String title;
    private Double minPrice;
    private Double maxDiscount;
    private Double amount; // for FLAT
    private Double percent; // for PERCENT
    private Boolean stackable = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime usableFrom;
    private LocalDateTime usableTo;
    @Column(nullable = false)
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    public Double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(Double maxDiscount) { this.maxDiscount = maxDiscount; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Double getPercent() { return percent; }
    public void setPercent(Double percent) { this.percent = percent; }
    public Boolean getStackable() { return stackable; }
    public void setStackable(Boolean stackable) { this.stackable = stackable; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUsableFrom() { return usableFrom; }
    public void setUsableFrom(LocalDateTime usableFrom) { this.usableFrom = usableFrom; }
    public LocalDateTime getUsableTo() { return usableTo; }
    public void setUsableTo(LocalDateTime usableTo) { this.usableTo = usableTo; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
