package com.boardly.data.repository;

import com.boardly.data.model.authentication.UserDevice;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserDeviceRepository extends CrudRepository<UserDevice, UUID> {
    void deleteByRefreshToken(String refreshToken);
}
