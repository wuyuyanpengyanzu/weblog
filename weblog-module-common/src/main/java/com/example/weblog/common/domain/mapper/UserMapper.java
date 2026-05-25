package com.example.weblog.common.domain.mapper;

import com.example.weblog.common.domain.dos.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 114514
* @description 针对表【t_user(用户表)】的数据库操作Mapper
* @createDate 2026-05-23 01:56:59
* @Entity com.example.weblog.common.domain.dos.User
*/
public interface UserMapper extends BaseMapper<User> {

}




