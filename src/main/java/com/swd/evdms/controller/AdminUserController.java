package com.swd.evdms.controller;

import com.swd.evdms.dto.request.UserCreateRequest;
import com.swd.evdms.dto.request.UserPasswordUpdateRequest;
import com.swd.evdms.dto.request.UserRoleUpdateRequest;
import com.swd.evdms.dto.response.UserSummaryResponse;
import com.swd.evdms.entity.Role;
import com.swd.evdms.entity.User;
import com.swd.evdms.repository.RoleRepository;
import com.swd.evdms.repository.UserRepository;
import com.swd.evdms.security.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;

    public AdminUserController(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder, AuthUtil authUtil) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.authUtil = authUtil;
    }

    private void requireManager() {
        var u = authUtil.getCurrentUser();
        var rn = u.getRole() != null ? u.getRole().getRoleName() : "";
        if (!"manager".equalsIgnoreCase(rn)) throw new RuntimeException("Access denied");
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> list() {
        requireManager();
        List<UserSummaryResponse> out = userRepo.findAll().stream()
                .map(u -> new UserSummaryResponse(u.getUserId(), u.getName(), u.getEmail(), u.getRole()!=null?u.getRole().getRoleName():null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PostMapping
    public ResponseEntity<UserSummaryResponse> create(@RequestBody UserCreateRequest req) {
        requireManager();
        if (userRepo.existsByEmail(req.getEmail())) throw new RuntimeException("Email already used");
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setPhoneNumber(req.getPhoneNumber());
        u.setAddress(req.getAddress());
        Integer rid = (req.getRoleId()!=null ? req.getRoleId() : 2); // 1=manager, 2=staff (seeded in data-h2)
        Role r = roleRepo.findById(rid).orElseThrow(() -> new RuntimeException("Role not found"));
        u.setRole(r);
        User saved = userRepo.save(u);
        return ResponseEntity.ok(new UserSummaryResponse(saved.getUserId(), saved.getName(), saved.getEmail(), r.getRoleName()));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserSummaryResponse> setRole(@PathVariable Integer id, @RequestBody UserRoleUpdateRequest req) {
        requireManager();
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Role r = roleRepo.findById(req.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
        u.setRole(r);
        User saved = userRepo.save(u);
        return ResponseEntity.ok(new UserSummaryResponse(saved.getUserId(), saved.getName(), saved.getEmail(), r.getRoleName()));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> setPassword(@PathVariable Integer id, @RequestBody UserPasswordUpdateRequest req) {
        requireManager();
        if (req.getPassword() == null || req.getPassword().isBlank()) throw new RuntimeException("Password required");
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepo.save(u);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        requireManager();
        userRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

