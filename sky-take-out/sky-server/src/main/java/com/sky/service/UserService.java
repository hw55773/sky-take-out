package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * interfaceName:       UserService
 * author:            wenhao2002
 * date:               2024/6/5 16:40
 */
public interface UserService {
    User login(UserLoginDTO userLoginDTO);
}
