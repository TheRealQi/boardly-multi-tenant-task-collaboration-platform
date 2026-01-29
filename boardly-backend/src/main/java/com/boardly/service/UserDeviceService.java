package com.boardly.service;

import com.boardly.data.model.User;
import com.boardly.data.model.UserDevice;
import com.boardly.data.repository.UserDeviceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class UserDeviceService {
    private UserDeviceRepository userDeviceRepository;

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
}
