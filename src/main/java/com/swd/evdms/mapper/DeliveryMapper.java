package com.swd.evdms.mapper;

import com.swd.evdms.dto.request.DeliveryRequest;
import com.swd.evdms.dto.response.DeliveryResponse;
import com.swd.evdms.entity.Delivery;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    @Mapping(source = "order.id",   target = "orderId")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicleUnit.id", target = "vehicleUnitId")
    // ĐỪNG dùng vehicle.name nếu ElectricVehicle không có field này!
    // Nếu muốn hiển thị tên xe, tạm map từ order.brand (có thật trong Order):
    @Mapping(source = "order.brand", target = "vehicleName")
    @Mapping(source = "order.user.name", target = "staffName")
    DeliveryResponse toResponse(Delivery delivery);

    List<DeliveryResponse> toResponses(List<Delivery> deliveries);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "order",        ignore = true)
    @Mapping(target = "vehicle",      ignore = true)
    @Mapping(target = "vehicleUnit",  ignore = true)
    @Mapping(target = "deliveryDate", ignore = true)
    Delivery toEntity(DeliveryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "order",        ignore = true)
    @Mapping(target = "vehicle",      ignore = true)
    @Mapping(target = "vehicleUnit",  ignore = true)
    @Mapping(target = "deliveryDate", ignore = true)
    void updateEntity(@MappingTarget Delivery target, DeliveryRequest source);
}
