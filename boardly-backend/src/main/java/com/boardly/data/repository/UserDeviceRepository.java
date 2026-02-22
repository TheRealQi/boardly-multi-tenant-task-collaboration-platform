package com.boardly.data.repository;

import com.boardly.data.model.sql.authentication.UserDevice;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository extends CrudRepository<UserDevice, UUID> {
    Optional<UserDevice> findByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);
}
