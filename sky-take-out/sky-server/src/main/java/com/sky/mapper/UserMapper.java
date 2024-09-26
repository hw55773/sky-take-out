package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * interfaceName:       UserMapper
 * author:            wenhao2002
 * date:               2024/6/5 21:36
 */
@Mapper
public interface UserMapper {
    @Select("select * from user where openid =#{openid}")
    User getByOpenId(String openid);

    void insert(User user);

    @Select("select * from user where id=#{userId}")
    User getById(Long userId);
}
