package com.swd.evdms.dto.response;

public class VehicleResponse {
    private Long id;
    private String model;
    private String brand;
    private String status;
    private String batteryCapacity;

    public VehicleResponse() {}

    public VehicleResponse(Long id, String model, String brand, String status, String batteryCapacity) {
        this.id = id;
        this.model = model;
        this.brand = brand;
        this.status = status;
        this.batteryCapacity = batteryCapacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(String batteryCapacity) { this.batteryCapacity = batteryCapacity; }
}

