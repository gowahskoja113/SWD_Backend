package com.swd.evdms.config;

import com.swd.evdms.entity.Brand;
import com.swd.evdms.entity.ElectricVehicle;
import com.swd.evdms.entity.Role;
import com.swd.evdms.repository.BrandRepository;
import com.swd.evdms.repository.ElectricVehicleRepository;
import com.swd.evdms.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class StartupDataConfig {

    @Bean
    CommandLineRunner ensureDefaults(RoleRepository roleRepository,
                                     BrandRepository brandRepository,
                                     ElectricVehicleRepository evRepository) {
        return args -> {
            // Roles
            boolean hasManager = roleRepository.findAll().stream()
                    .anyMatch(r -> r.getRoleName() != null && r.getRoleName().equalsIgnoreCase("manager"));
            boolean hasStaff = roleRepository.findAll().stream()
                    .anyMatch(r -> r.getRoleName() != null && r.getRoleName().equalsIgnoreCase("staff"));

            if (!hasManager) {
                Role manager = new Role();
                manager.setRoleName("manager");
                manager.setDescription("Manager");
                roleRepository.save(manager);
            }
            if (!hasStaff) {
                Role staff = new Role();
                staff.setRoleName("staff");
                staff.setDescription("Staff");
                roleRepository.save(staff);
            }

            // Brand + Models (seed if empty)
            if (evRepository.count() == 0) {
                Brand brand = brandRepository.findByName("DemoBrand").orElseGet(() -> {
                    Brand b = new Brand();
                    b.setName("DemoBrand");
                    return brandRepository.save(b);
                });

                ElectricVehicle evS = new ElectricVehicle();
                evS.setModel("EV S");
                evS.setCost(new BigDecimal("500000000"));
                evS.setPrice(new BigDecimal("800000000"));
                evS.setStatus("AVAILABLE");
                evS.setBatteryCapacity("90kWh");
                evS.setBrand(brand);
                evRepository.save(evS);

                ElectricVehicle evX = new ElectricVehicle();
                evX.setModel("EV X");
                evX.setCost(new BigDecimal("700000000"));
                evX.setPrice(new BigDecimal("1200000000"));
                evX.setStatus("AVAILABLE");
                evX.setBatteryCapacity("100kWh");
                evX.setBrand(brand);
                evRepository.save(evX);
            }
        };
    }
}
