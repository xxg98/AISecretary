package com.kailei.aisecretary.service;

import com.kailei.aisecretary.dto.UserEmailRegisterDto;
import com.kailei.aisecretary.entity.UsersEntity;
import com.kailei.aisecretary.vo.UsersLoginVo;

public interface IUserService {
    UsersEntity selectUserById(Long id);
    //用户注册
    boolean userRegister(UsersEntity usersEntity);
    //用户登录
    UsersLoginVo userLogin(UsersEntity usersEntity);
    //获取邮箱验证码
    String getEmailCode(String email);
    //邮箱注册 接收UserEmailRegisterDto参数
    boolean userEmailRegister(UserEmailRegisterDto userEmailRegisterDto);

}