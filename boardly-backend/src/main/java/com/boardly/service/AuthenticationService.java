package com.boardly.service;

import com.boardly.commmon.dto.LoginRequestDTO;
import com.boardly.commmon.dto.LoginResponseDTO;
import com.boardly.commmon.dto.RegisterRequestDTO;
import com.boardly.data.model.User;
import com.boardly.data.repository.UserDeviceRepository;
import com.boardly.data.repository.UserRepository;
import com.boardly.security.model.AppUserDetails;
import com.boardly.security.service.JWTFilterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTFilterService jwtFilterService;
    private final UserDeviceService userDeviceService;

    @Autowired
    public AuthenticationService(UserRepository userRepository, UserDeviceRepository userDeviceRepository, AuthenticationManager authenticationManager, JWTFilterService jwtFilterService, UserDeviceService userDeviceService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtFilterService = jwtFilterService;
        this.userDeviceService = userDeviceService;
    }

    public boolean register(RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        String email = registerRequestDTO.getEmail();
        String password = registerRequestDTO.getPassword();

        String firstName = registerRequestDTO.getFirstName();
        String lastName = registerRequestDTO.getLastName();

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
