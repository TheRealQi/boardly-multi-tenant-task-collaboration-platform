package com.boardly.service;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.LoginRequestDTO;
import com.boardly.commmon.dto.LoginResponseDTO;
import com.boardly.commmon.dto.RegisterRequestDTO;
import com.boardly.data.model.User;
import com.boardly.data.repository.UserDeviceRepository;
import com.boardly.data.repository.UserRepository;
import com.boardly.exception.FieldsValidationException;
import com.boardly.security.model.AppUserDetails;
import com.boardly.security.service.JWTFilterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTFilterService jwtFilterService;
    private final UserDeviceService userDeviceService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(UserRepository userRepository, UserDeviceRepository userDeviceRepository, AuthenticationManager authenticationManager, JWTFilterService jwtFilterService, UserDeviceService userDeviceService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtFilterService = jwtFilterService;
        this.userDeviceService = userDeviceService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public boolean register(RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        String email = registerRequestDTO.getEmail();
        String password = registerRequestDTO.getPassword();

        String firstName = registerRequestDTO.getFirstName();
        String lastName = registerRequestDTO.getLastName();

        FieldsValidationException validation = new FieldsValidationException();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Email is already taken");
        }

        if (validation.hasErrors()) {
            throw validation;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        userRepository.save(newUser);

        return true;
    }

    public LoginResponseDTO login(LoginRequestDTO request, HttpServletRequest servletRequest) {
        String usernameOrEmail = request.getUsernameOrEmail();
        String password = request.getPassword();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UUID userPublicId = userDetails.getUserPublicId();
        User user = userDetails.getUser();

        long expiresAt = jwtFilterService.getAccessTokenExpirationFromNow();
        String accessToken = jwtFilterService.generateToken(userPublicId, expiresAt);
        String refreshToken = jwtFilterService.generateRefreshToken(userPublicId);


        userDeviceService.captureUserDeviceInfo(user, refreshToken, servletRequest);

        return new LoginResponseDTO(userPublicId.toString(), accessToken, refreshToken, expiresAt);
    }
}
