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
import org.springframework.transaction.annotation.Transactional;

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
        // Guard: unit must be available at dealer
        if (unit.getStatus() == null || !"AT_DEALER".equalsIgnoreCase(unit.getStatus())) {
            throw new RuntimeException("Vehicle unit is not at dealer");
        }
        entity.setVehicleUnit(unit);
        entity.setVehicle(unit.getModel());

        // Guard: unit model must match order vehicle
        if (order.getVehicle() != null && unit.getModel() != null) {
            Long ov = order.getVehicle().getId();
            Long um = unit.getModel().getId();
            if (ov != null && um != null && !ov.equals(um)) {
                throw new RuntimeException("Selected vehicle unit does not match order model");
            }
        }

        // Always start as PENDING regardless of request (normalized)
        entity.setStatus("PENDING");
        entity.setDeliveryDate(java.time.LocalDateTime.now());

        // Prefill from order if not provided in request
        if (req.getCustomerName() != null && !req.getCustomerName().isBlank()) {
            entity.setCustomerName(req.getCustomerName());
        } else if (order.getCustomerInfo() != null && !order.getCustomerInfo().isBlank()) {
            entity.setCustomerName(order.getCustomerInfo());
        }

        if (order.getPriceAfter() != null && order.getPrice() != null) {
            entity.setPriceBefore(order.getPrice());
            entity.setDiscountApplied(order.getPrice().subtract(order.getPriceAfter()));
            entity.setPriceAfter(order.getPriceAfter());
        } else if (req.getPriceBefore() != null) {
            entity.setPriceBefore(req.getPriceBefore());
        } else if (order.getPrice() != null) {
            entity.setPriceBefore(order.getPrice());
        }

        if (req.getDeposit() != null) entity.setDeposit(req.getDeposit());
        if ((order.getPriceAfter() == null || order.getPrice() == null) && req.getVoucherCode() != null && !req.getVoucherCode().isBlank() && entity.getPriceBefore() != null) {
            Voucher v = voucherRepository.findByCodeIgnoreCase(req.getVoucherCode()).orElse(null);
            if (v != null && Boolean.TRUE.equals(v.getActive()) && isVoucherUsable(v)) {
                BigDecimal discount = computeDiscount(v, entity.getPriceBefore());
                entity.setDiscountApplied(discount);
                entity.setPriceAfter(entity.getPriceBefore().subtract(discount).max(BigDecimal.ZERO));
            } else {
                entity.setPriceAfter(entity.getPriceBefore());
            }
        } else if (entity.getPriceBefore() != null && entity.getPriceAfter() == null) {
            entity.setPriceAfter(entity.getPriceBefore());
        }

        return deliveryMapper.toResponse(deliveryRepository.save(entity));
    }

    @Transactional
    public DeliveryResponse updateStatus(Long id, String status) {
        var entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        // normalize target/current statuses
        String target = status == null ? "" : status.toUpperCase();
        String current = entity.getStatus() == null ? "" : entity.getStatus().toUpperCase();
        // prevent cancelling after delivered and enforce ownership for staff
        if ("CANCELLED".equals(target) && ("DELIVERED".equals(current) || "COMPLETED".equals(current))) {
            throw new RuntimeException("Cannot cancel a delivered delivery");
        }
        var me = authUtil.getCurrentUser();
        boolean isManager = me.getRole() != null && "manager".equalsIgnoreCase(me.getRole().getRoleName());
        if (!isManager) {
            if (entity.getOrder() == null || entity.getOrder().getUser() == null || !me.getUserId().equals(entity.getOrder().getUser().getUserId())) {
                throw new RuntimeException("Access denied");
            }
        }
        entity.setStatus(target);
        if ("DELIVERED".equals(target) || "COMPLETED".equals(target)) {
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
        } else if ("CANCELLED".equals(target)) {
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
                deliveryRepository.findByOrder_User_UserId(current.getUserId())
        );
    }
}
