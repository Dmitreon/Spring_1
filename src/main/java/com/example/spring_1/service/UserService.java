package com.example.spring_1.service;

import com.example.spring_1.dto.UserDto;
import com.example.spring_1.entity.User;

import java.util.List;

public interface UserService {
    User saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();

    void updatePassword(User user, String newPassword);
}
