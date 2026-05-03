package com.boardly.service;

import com.boardly.common.dto.UpdateUserProfileRequestDTO;
import com.boardly.common.dto.UserDTO;
import com.boardly.data.mapper.UserMapper;
import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.repository.UserRepository;
import com.boardly.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, UserMapper userMapper, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.fileStorageService = fileStorageService;
    }

    public UserDTO getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    public UserDTO updateUserProfile(UUID userId, UpdateUserProfileRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        return userMapper.toDTO(userRepository.save(user));
    }

    public String uploadProfilePicture(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String oldUri = user.getProfilePictureUri();

        String newUri = fileStorageService.storeFile(file);
        user.setProfilePictureUri(newUri);
        userRepository.save(user);

        fileStorageService.deleteFile(oldUri);

        return newUri;
    }
}