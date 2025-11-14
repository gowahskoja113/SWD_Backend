package com.swd.evdms.service;

import com.swd.evdms.dto.request.OrderRequest;
import com.swd.evdms.dto.response.OrderResponse;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.entity.Order;
import com.swd.evdms.entity.User;
import com.swd.evdms.entity.Voucher;
import com.swd.evdms.repository.VoucherRepository;
import com.swd.evdms.mapper.OrderMapper;
import com.swd.evdms.repository.ElectricVehicleRepository;
import com.swd.evdms.repository.OrderRepository;
import com.swd.evdms.repository.UserRepository;
import com.swd.evdms.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ElectricVehicleRepository vehicleRepository;
    private final OrderMapper orderMapper;
    private final AuthUtil authUtil;
    private final VoucherRepository voucherRepository;

    public OrderResponse createOrder(OrderRequest request) {
        User currentUser = authUtil.getCurrentUser(); // lấy user hiện tại
        Order order = orderMapper.toEntity(request);

        ElectricVehicle vehicle = vehicleRepository.findById
                        (request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        order.setUser(currentUser);
        order.setVehicle(vehicle);
        order.setStatus("PENDING");

        // compute priceAfter and discount if voucherCode provided
        if (request.getPrice() != null) {
            var price = request.getPrice();
            order.setPrice(price);
            if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
                Voucher v = voucherRepository.findByCodeIgnoreCase(request.getVoucherCode()).orElse(null);
                if (v != null && isVoucherUsable(v, price)) {
                    java.math.BigDecimal discount = computeDiscount(v, price);
                    order.setDiscountApplied(discount);
                    order.setPriceAfter(price.subtract(discount).max(java.math.BigDecimal.ZERO));
                    order.setVoucherCode(v.getCode());
                } else {
                    order.setPriceAfter(price);
                }
            } else {
                order.setPriceAfter(price);
            }
        }

        return orderMapper.toResponse(orderRepository.save(order));
    }

    private boolean isVoucherUsable(Voucher v, java.math.BigDecimal price) {
        var now = java.time.LocalDateTime.now();
        if (v.getUsableFrom() != null && now.isBefore(v.getUsableFrom())) return false;
        if (v.getUsableTo() != null && now.isAfter(v.getUsableTo())) return false;
        if (!Boolean.TRUE.equals(v.getStackable()) && v.getMaxDiscount() != null && v.getMaxDiscount() < 0) return false;
        if (v.getMinPrice() != null && price.compareTo(java.math.BigDecimal.valueOf(v.getMinPrice())) < 0) return false;
        return v.getCode() != null && (v.getActive() == null || v.getActive());
    }

    private java.math.BigDecimal computeDiscount(Voucher v, java.math.BigDecimal price) {
        java.math.BigDecimal d = java.math.BigDecimal.ZERO;
        if ("FLAT".equalsIgnoreCase(v.getType()) && v.getAmount() != null) {
            d = java.math.BigDecimal.valueOf(v.getAmount());
        }
        if ("PERCENT".equalsIgnoreCase(v.getType()) && v.getPercent() != null) {
            d = price.multiply(java.math.BigDecimal.valueOf(v.getPercent())).divide(java.math.BigDecimal.valueOf(100));
        }
        if (v.getMaxDiscount() != null) d = d.min(java.math.BigDecimal.valueOf(v.getMaxDiscount()));
        if (d.signum() < 0) d = java.math.BigDecimal.ZERO;
        return d;
    }

    // ✅ Lấy tất cả đơn hàng (manager có thể dùng)
    public List<OrderResponse> getAllOrders() {
        User current = authUtil.getCurrentUser();
        String role = current.getRole() != null ? current.getRole().getRoleName() : "";
        boolean isManager = "manager".equalsIgnoreCase(role);
        return (isManager ? orderRepository.findAll() : orderRepository.findByUser_UserId(current.getUserId()))
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Lấy đơn hàng của user hiện tại
    public List<OrderResponse> getOrdersOfCurrentUser() {
        User currentUser = authUtil.getCurrentUser();
        return orderRepository.findByUser_UserId(currentUser.getUserId())
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Lấy đơn hàng theo ID (có kiểm tra quyền)
    public OrderResponse getOrderById(Long id) {
        User current = authUtil.getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        String role = current.getRole() != null ? current.getRole().getRoleName() : "";
        boolean isManager = "manager".equalsIgnoreCase(role);
        if (!isManager && (order.getUser() == null || !current.getUserId().equals(order.getUser().getUserId()))) {
            throw new RuntimeException("Access denied");
        }
        return orderMapper.toResponse(order);
    }

    // ✅ Cập nhật trạng thái (manager hoặc staff)
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // ✅ Xóa đơn hàng
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
