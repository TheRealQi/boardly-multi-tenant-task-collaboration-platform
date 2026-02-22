package com.boardly.service;

import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.model.sql.authentication.UserDevice;
import com.boardly.data.repository.UserDeviceRepository;
import com.boardly.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;

    @Autowired
    public UserDeviceService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    @Async
    public void captureUserDeviceInfo(User user, String refreshToken, HttpServletRequest servletRequest) {
        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = servletRequest.getRemoteAddr();
        Instant lastLoggedInOn = Instant.now();
        Instant refreshTokenExpiresOn = Instant.now().plusSeconds(60L * 60L * 24L * 30L); // 30 days
        userDeviceRepository.save(new UserDevice(user, userAgent, ipAddress, lastLoggedInOn, refreshToken, refreshTokenExpiresOn, null));
    }

    public UserDevice findAndVerifyRefreshToken(String refreshToken) {
        UserDevice userDevice = userDeviceRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (userDevice.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
            userDeviceRepository.delete(userDevice);
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }
        return userDevice;
    }

    public void rotateRefreshToken(UserDevice userDevice, String newRefreshToken) {
        userDevice.setRefreshedOn(Instant.now());
        userDevice.setRefreshToken(newRefreshToken);
        userDevice.setRefreshTokenExpiresAt(Instant.now().plusSeconds(60L * 60L * 24L * 30L)); // 30 days
        userDeviceRepository.save(userDevice);
    }

    public void deleteByRefreshToken(String refreshToken) {
        userDeviceRepository.deleteByRefreshToken(refreshToken);
    }
}
