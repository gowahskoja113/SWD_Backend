package com.swd.evdms.service;

import com.swd.evdms.dto.request.AuthRequest;
import com.swd.evdms.dto.request.RegisterRequest;
import com.swd.evdms.dto.response.AuthResponse;
import com.swd.evdms.dto.response.RegisterResponse;
import com.swd.evdms.entity.Role;
import com.swd.evdms.entity.User;
import com.swd.evdms.mapper.AuthMapper;
import com.swd.evdms.repository.RoleRepository;
import com.swd.evdms.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JpaUserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       AuthMapper authMapper,
                       PasswordEncoder passwordEncoder,
                       JpaUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already used");
        }

        User user = authMapper.toEntity(req);

        // Nếu không truyền roleId thì mặc định = 1 (USER)
        Integer roleId = (req.getRoleId() != null) ? req.getRoleId() : 1;

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id " + roleId));

        user.setRole(role);

        // mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);

        return new RegisterResponse(
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                role.getRoleName()
        );
    }

    public AuthResponse login(AuthRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // load UserDetails từ DB
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.getEmail());

        // generate token dựa vào UserDetails
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, req.getEmail());
    }
}
