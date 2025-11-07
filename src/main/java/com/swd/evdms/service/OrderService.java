package com.swd.evdms.service;

import com.swd.evdms.dto.request.OrderRequest;
import com.swd.evdms.dto.response.OrderResponse;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.entity.Order;
import com.swd.evdms.entity.User;
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

    public OrderResponse createOrder(OrderRequest request) {
        User currentUser = authUtil.getCurrentUser(); // lấy user hiện tại
        Order order = orderMapper.toEntity(request);

        ElectricVehicle vehicle = vehicleRepository.findById
                        (request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        order.setUser(currentUser);
        order.setVehicle(vehicle);
        order.setStatus("PENDING");

        return orderMapper.toResponse(orderRepository.save(order));
    }

    // ✅ Lấy tất cả đơn hàng (manager có thể dùng)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
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
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
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
