package com.boardly.service;

import com.boardly.common.dto.authentication.*;
import com.boardly.common.enums.TokenType;
import com.boardly.data.model.sql.authentication.SecureToken;
import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.model.sql.authentication.UserDevice;
import com.boardly.data.repository.SecureTokenRepository;
import com.boardly.data.repository.UserDeviceRepository;
import com.boardly.data.repository.UserRepository;
import com.boardly.exception.FieldsValidationException;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import com.boardly.security.service.JWTFilterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTFilterService jwtFilterService;
    private final UserDeviceService userDeviceService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureTokenRepository secureTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    public AuthenticationService(UserRepository userRepository, UserDeviceRepository userDeviceRepository, AuthenticationManager authenticationManager, JWTFilterService jwtFilterService, UserDeviceService userDeviceService, PasswordEncoder passwordEncoder, EmailService emailService, SecureTokenRepository secureTokenRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtFilterService = jwtFilterService;
        this.userDeviceService = userDeviceService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.secureTokenRepository = secureTokenRepository;
    }

    @Transactional
    public void register(RegisterRequestDTO registerRequestDTO) {
        logger.info("Registering user: {}", registerRequestDTO.getUsername());
        String username = registerRequestDTO.getUsername();
        String email = registerRequestDTO.getEmail();
        String password = registerRequestDTO.getPassword();

        String firstName = registerRequestDTO.getFirstName();
        String lastName = registerRequestDTO.getLastName();

        FieldsValidationException validation = new FieldsValidationException();

        if (userRepository.existsByEmail(email)) {
            validation.addError("email", "Email Address is already taken");
        }

        if (userRepository.existsByUsername(username)) {
            validation.addError("username", "Username is already taken");
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
        logger.info("User registered successfully: {}", username);

        sendEmailVerificationEmail(newUser);
    }

    public LoginResponseDTO login(LoginRequestDTO request, HttpServletRequest servletRequest) {
        logger.info("Logging in user: {}", request.getUsernameOrEmail());
        String usernameOrEmail = request.getUsernameOrEmail();
        String password = request.getPassword();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();
        User user = userDetails.getUser();

        String accessToken = jwtFilterService.generateToken(userId);
        String refreshToken = jwtFilterService.generateRefreshToken(userId);
        long expiresAt = jwtFilterService.getAccessTokenExpirationFromNow();

        userDeviceService.captureUserDeviceInfo(user, refreshToken, servletRequest);
        logger.info("User logged in successfully: {}", user.getUsername());

        return new LoginResponseDTO(userId, accessToken, refreshToken, expiresAt);
    }

    @Transactional
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        logger.info("Refreshing token");
        UserDevice userDevice = userDeviceService.findAndVerifyRefreshToken(request.getRefreshToken());
        User user = userDevice.getUser();

        String newAccessToken = jwtFilterService.generateToken(user.getId());
        String newRefreshToken = jwtFilterService.generateRefreshToken(user.getId());
        long expiresAt = jwtFilterService.getAccessTokenExpirationFromNow();

        userDeviceService.rotateRefreshToken(userDevice, newRefreshToken);
        logger.info("Token refreshed successfully for user: {}", user.getUsername());

        return new LoginResponseDTO(user.getId(), newAccessToken, newRefreshToken, expiresAt);
    }

    @Transactional
    public void logout(RefreshTokenRequestDTO request) {
        logger.info("Logging out user");
        userDeviceService.deleteByRefreshToken(request.getRefreshToken());
        logger.info("User logged out successfully");
    }

    @Transactional
    public void sendEmailVerificationEmail(User user) {
        if (user.isEmailVerified()) {
            logger.debug("User {} is already verified", user.getUsername());
            return;
        }
        String token = UUID.randomUUID().toString();
        SecureToken secureToken = new SecureToken();
        secureToken.setToken(token);
        secureToken.setUser(user);
        secureToken.setTokenType(TokenType.EMAIL_VERIFICATION);
        secureToken.setExpiresAt(java.time.Instant.now().plusSeconds(86400)); // 24 hours
        secureTokenRepository.deleteAllByUserAndTokenType(user, TokenType.EMAIL_VERIFICATION);
        secureTokenRepository.save(secureToken);
        emailService.sendEmailVerificationEmail(user.getEmail(), token);
        logger.info("Sent email verification to {}", user.getEmail());
    }

    @Transactional
    public void verifyEmailAddress(String token) {
        logger.info("Verifying email with token: {}", token);
        SecureToken secureToken = secureTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (secureToken.isExpired()) {
            secureTokenRepository.delete(secureToken);
            throw new ResourceNotFoundException("Invalid token");
        }

        if (secureToken.getTokenType() != TokenType.EMAIL_VERIFICATION) {
            throw new ResourceNotFoundException("Invalid token");
        }

        User user = secureToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        logger.info("Email verified successfully for user: {}", user.getUsername());

        secureTokenRepository.deleteAllByUserAndTokenType(user, TokenType.EMAIL_VERIFICATION);
    }

    @Transactional
    public void processForgotPassword(String email) {
        logger.info("Processing forgot password for email: {}", email);
        userRepository.findByEmail(email).ifPresent(user -> {
            secureTokenRepository.deleteAllByUserAndTokenType(user, TokenType.PASSWORD_RESET);

            String token = UUID.randomUUID().toString();
            SecureToken secureToken = new SecureToken();
            secureToken.setToken(token);
            secureToken.setUser(user);
            secureToken.setTokenType(TokenType.PASSWORD_RESET);
            secureToken.setExpiresAt(Instant.now().plusSeconds(1800));
            secureTokenRepository.save(secureToken);

            emailService.sendPasswordResetEmail(user.getEmail(), token);
            logger.info("Sent password reset email to {}", user.getEmail());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request, String token) {
        logger.info("Resetting password with token: {}", token);
        SecureToken secureToken = secureTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));
        if (secureToken.isExpired()) {
            secureTokenRepository.delete(secureToken);
            throw new ResourceNotFoundException("Invalid token");
        }
        if (secureToken.getTokenType() != TokenType.PASSWORD_RESET) {
            throw new ResourceNotFoundException("Invalid token");
        }
        User user = secureToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        secureTokenRepository.deleteAllByUserAndTokenType(user, TokenType.PASSWORD_RESET);
        userRepository.save(user);
        logger.info("Password reset successfully for user: {}", user.getUsername());
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDTO request, AppUserDetails appUserDetails) {
        logger.info("Changing password for user: {}", appUserDetails.getUsername());
        User currentUser = appUserDetails.getUser();
        FieldsValidationException validation = new FieldsValidationException();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPasswordHash())) {
            validation.addError("oldPassword", "Old password is incorrect");
        }
        if (validation.hasErrors()) {
            throw validation;
        }
        currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        logger.info("Password changed successfully for user: {}", appUserDetails.getUsername());
    }
}
