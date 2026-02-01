package com.boardly.data.mapper;

import com.boardly.commmon.dto.UserDTO;
import com.boardly.data.model.authentication.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
}
