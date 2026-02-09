package com.boardly.data.mapper;

import com.boardly.commmon.dto.UserDTO;
import com.boardly.data.model.sql.authentication.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "profilePictureUrl", source = "user.profilePictureUri")
    UserDTO toDTO(User user);
}
