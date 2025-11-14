package com.swd.evdms.service;


import com.swd.evdms.dto.request.DeliveryRequest;
import com.swd.evdms.dto.response.DeliveryResponse;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.mapper.DeliveryMapper;
import com.swd.evdms.repository.DeliveryRepository;
import com.swd.evdms.repository.ElectricVehicleRepository;
import com.swd.evdms.repository.VehicleUnitRepository;
import com.swd.evdms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import com.swd.evdms.entity.Voucher;
import com.swd.evdms.repository.VoucherRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final ElectricVehicleRepository vehicleRepository;
    private final DeliveryMapper deliveryMapper;
    private final VehicleUnitRepository unitRepository;
    private final VoucherRepository voucherRepository;
    private final com.swd.evdms.security.AuthUtil authUtil;

    public DeliveryResponse create(DeliveryRequest req) {
        var entity = deliveryMapper.toEntity(req);

        var order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        entity.setOrder(order);
        if (deliveryRepository.existsByOrder_Id(order.getId())) {
            throw new RuntimeException("Order already has delivery");
        }

        if (req.getVehicleUnitId() == null) {
            throw new RuntimeException("vehicleUnitId is required");
        }
        var unit = unitRepository.findById(req.getVehicleUnitId())
                .orElseThrow(() -> new RuntimeException("Vehicle unit not found"));
        entity.setVehicleUnit(unit);
        entity.setVehicle(unit.getModel());

        // Always start as Pending regardless of request
        entity.setStatus("Pending");
        entity.setDeliveryDate(java.time.LocalDateTime.now());

        // Prefill from order if not provided in request
        if (req.getCustomerName() != null && !req.getCustomerName().isBlank()) {
            entity.setCustomerName(req.getCustomerName());
        } else if (order.getCustomerInfo() != null && !order.getCustomerInfo().isBlank()) {
            entity.setCustomerName(order.getCustomerInfo());
        }

        if (req.getPriceBefore() != null) {
            entity.setPriceBefore(req.getPriceBefore());
        } else if (order.getPrice() != null) {
            entity.setPriceBefore(order.getPrice());
        }

        if (req.getDeposit() != null) entity.setDeposit(req.getDeposit());
        if (req.getVoucherCode() != null && !req.getVoucherCode().isBlank() && entity.getPriceBefore() != null) {
            Voucher v = voucherRepository.findByCodeIgnoreCase(req.getVoucherCode()).orElse(null);
            if (v != null && Boolean.TRUE.equals(v.getActive()) && isVoucherUsable(v)) {
                BigDecimal discount = computeDiscount(v, entity.getPriceBefore());
                entity.setDiscountApplied(discount);
                entity.setPriceAfter(entity.getPriceBefore().subtract(discount).max(BigDecimal.ZERO));
            } else {
                entity.setPriceAfter(entity.getPriceBefore());
            }
        } else if (entity.getPriceBefore() != null) {
            entity.setPriceAfter(entity.getPriceBefore());
        }

        return deliveryMapper.toResponse(deliveryRepository.save(entity));
    }

    public DeliveryResponse updateStatus(Long id, String status) {
        var entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        // prevent cancelling after delivered
        String current = entity.getStatus();
        if ("Cancelled".equalsIgnoreCase(status) && ("Delivered".equalsIgnoreCase(current) || "COMPLETED".equalsIgnoreCase(current))) {
            throw new RuntimeException("Cannot cancel a delivered delivery");
        }
        entity.setStatus(status);
        if ("Delivered".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            if (entity.getVehicleUnit() != null) {
                var unit = entity.getVehicleUnit();
                unit.setStatus("DELIVERED");
                unit.setDeliveredAt(java.time.LocalDateTime.now());
                unitRepository.save(unit);
            }
            if (entity.getOrder() != null) {
                var o = entity.getOrder();
                o.setStatus("COMPLETED");
                orderRepository.save(o);
            }
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            // If cancelled, keep vehicle at current state (likely AT_DEALER). No-op.
        }
        return deliveryMapper.toResponse(deliveryRepository.save(entity));
    }

    private boolean isVoucherUsable(Voucher v) {
        var now = java.time.LocalDateTime.now();
        if (v.getUsableFrom() != null && now.isBefore(v.getUsableFrom())) return false;
        if (v.getUsableTo() != null && now.isAfter(v.getUsableTo())) return false;
        return true;
    }

    private BigDecimal computeDiscount(Voucher v, BigDecimal price) {
        if (price == null || price.signum() <= 0) return BigDecimal.ZERO;
        if (v.getMinPrice() != null && price.compareTo(BigDecimal.valueOf(v.getMinPrice())) < 0) return BigDecimal.ZERO;
        BigDecimal d = BigDecimal.ZERO;
        if ("FLAT".equalsIgnoreCase(v.getType()) && v.getAmount() != null) {
            d = BigDecimal.valueOf(v.getAmount());
        }
        if ("PERCENT".equalsIgnoreCase(v.getType()) && v.getPercent() != null) {
            d = price.multiply(BigDecimal.valueOf(v.getPercent())).divide(BigDecimal.valueOf(100));
        }
        if (v.getMaxDiscount() != null) d = d.min(BigDecimal.valueOf(v.getMaxDiscount()));
        if (d.signum() < 0) d = BigDecimal.ZERO;
        return d;
    }

    public List<DeliveryResponse> listAll() {
        var current = authUtil.getCurrentUser();
        String role = current.getRole() != null ? String.valueOf(current.getRole().getRoleName()) : "";
        boolean isManager = "manager".equalsIgnoreCase(role);
        if (isManager) {
            return deliveryMapper.toResponses(deliveryRepository.findAll());
        }
        return deliveryMapper.toResponses(
                deliveryRepository.findAll().stream()
                        .filter(d -> d.getOrder() != null && d.getOrder().getUser() != null
                                && d.getOrder().getUser().getUserId().equals(current.getUserId()))
                        .toList()
        );
    }
}
