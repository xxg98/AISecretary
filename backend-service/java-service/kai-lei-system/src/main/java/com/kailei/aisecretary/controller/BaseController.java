package com.kailei.aisecretary.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.kailei.aisecretary.entity.UsersEntity;
import com.kailei.aisecretary.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseController {

    @Autowired
    private IUserService iUserService;

    /**
     * 获取用户名
     */
    public String getUserName(){
        // 解密token变成id
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        System.out.println("loginIdAsLong = " + loginIdAsLong);
        UsersEntity usersEntity = iUserService.selectUserById(loginIdAsLong);
        return usersEntity.getName();
    }

}
