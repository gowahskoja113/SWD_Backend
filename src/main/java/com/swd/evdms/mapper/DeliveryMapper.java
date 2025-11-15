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
    // Hiển thị mẫu xe: ưu tiên Brand + Model từ vehicle, fallback sang order.brand
    @Mapping(target = "vehicleName", expression = "java(buildVehicleName(delivery))")
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

    default String buildVehicleName(Delivery delivery) {
        if (delivery == null) return null;
        String brand = null;
        String model = null;
        if (delivery.getVehicle() != null) {
            model = delivery.getVehicle().getModel();
            if (delivery.getVehicle().getBrand() != null) {
                brand = delivery.getVehicle().getBrand().getName();
            }
        }
        if ((brand == null || brand.isBlank()) && delivery.getOrder() != null) {
            brand = delivery.getOrder().getBrand();
        }
        if (brand == null || brand.isBlank()) return model;
        if (model == null || model.isBlank()) return brand;
        return brand + " " + model;
    }
}
