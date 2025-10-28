package com.swd.evdms.mapper;


import com.swd.evdms.dto.request.OrderRequest;
import com.swd.evdms.dto.response.OrderResponse;
import com.swd.evdms.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Ánh xạ từ entity sang response
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.name", target = "username") // đổi username -> name
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.model", target = "vehicleModel")
    OrderResponse toResponse(Order order);

    // Ánh xạ từ request -> entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // user sẽ được set trong service
    @Mapping(target = "delivery", ignore = true)
    Order toEntity(OrderRequest request);

    // Convert Instant -> LocalDateTime (nếu có dùng)
    default LocalDateTime map(Instant instant) {
        return instant == null ? null :
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}