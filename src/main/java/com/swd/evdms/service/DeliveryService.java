package com.swd.evdms.service;


import com.swd.evdms.dto.request.DeliveryRequest;
import com.swd.evdms.dto.response.DeliveryResponse;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.mapper.DeliveryMapper;
import com.swd.evdms.repository.DeliveryRepository;
import com.swd.evdms.repository.ElectricVehicleRepository;
import com.swd.evdms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final ElectricVehicleRepository vehicleRepository;
    private final DeliveryMapper deliveryMapper;

    public DeliveryResponse create(DeliveryRequest req) {
        var entity = deliveryMapper.toEntity(req);

        var order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        entity.setOrder(order);

        if (req.getVehicleId() != null) {
            var vehicle = vehicleRepository.findById(req.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            entity.setVehicle(vehicle);
        } else {
            // Nếu có field available:
            var vehicle = (req.getVehicleId() != null)
                    ? vehicleRepository.findById(req.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"))
                    : vehicleRepository.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("No available vehicle"));
            entity.setVehicle(vehicle);
        }

        if (entity.getStatus() == null) entity.setStatus("Pending");
        entity.setDeliveryDate(java.time.LocalDateTime.now());

        return deliveryMapper.toResponse(deliveryRepository.save(entity));
    }

    public DeliveryResponse updateStatus(Long id, String status) {
        var entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        entity.setStatus(status);
        return deliveryMapper.toResponse(deliveryRepository.save(entity));
    }

    public List<DeliveryResponse> listAll() {
        return deliveryMapper.toResponses(deliveryRepository.findAll());
    }
}
