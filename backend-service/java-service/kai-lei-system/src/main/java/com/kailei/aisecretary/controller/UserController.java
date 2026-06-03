package com.kailei.aisecretary.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.kailei.aisecretary.config.SystemConfig;
import com.kailei.aisecretary.dto.UserEmailRegisterDto;
import com.kailei.aisecretary.dto.UsersDto;
import com.kailei.aisecretary.dto.UsersRegisterDto;
import com.kailei.aisecretary.entity.UsersEntity;
import com.kailei.aisecretary.filter.WordFilter;
import com.kailei.aisecretary.service.IUserService;
import com.kailei.aisecretary.utils.RedisUtil;
import com.kailei.aisecretary.utils.Result;
import com.kailei.aisecretary.vo.UsersLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户接口")
public class UserController extends BaseController {

    @Autowired
    private IUserService iUserService;

    @Value("${users.name}")
    private String userName;

    @Value("${users.open}")
    private boolean open;

    @Autowired
    private SystemConfig systemConfig;

    @Resource
    private RedisUtil redisUtil;

    //获取当前登录用户信息
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/getUserInfo")
    public Result getUserInfo(){
        // 获取当前登录人的id拼接成一个唯一key
        String key = "user:info:"+ StpUtil.getLoginIdAsLong();
        if(redisUtil.hasKey(key)){ //判断key是否存在
            String userName = (String) redisUtil.get(key);
            log.info("这个数据走redis获取的");
            return Result.success(userName);
        }else{
            // 如果redis没有去数据库获取，如果有就直接返回
            String userName = getUserName();
            redisUtil.set(key,userName,60);
            log.info("这个数据走mysql获取的");
            return Result.success(userName);
        }
    }

    @Operation(summary = "用户登录")
    @PostMapping("/userLogin")
    public Result<UsersLoginVo> userLogin(@RequestBody UsersDto usersDto){
        // 拷贝的前提是属性名一致
        UsersEntity userEntity = BeanUtil.copyProperties(usersDto, UsersEntity.class);
        UsersLoginVo usersLoginVo = iUserService.userLogin(userEntity);
        //返回的时候返回token和昵称
        return Result.success(usersLoginVo);
    }


    /**
     * 用户注册
     * @return
     */
    @Operation(summary = "用户名密码注册")
    @PostMapping("/passRegister")
    public Result passRegister(@Valid @RequestBody UsersRegisterDto usersDto){
        if (WordFilter.violationInspection(usersDto.getName())){
            return Result.fail("用户名违规");
        }
        // 拷贝的前提是属性名一致
        UsersEntity userEntity = BeanUtil.copyProperties(usersDto, UsersEntity.class);
        iUserService.userRegister(userEntity);
        return Result.success("注册成功");
    }

//    @Operation(summary = "获取用户名")
//    @GetMapping("/getUserName")
//    public String getUserName(){
//        if(open){
//            return "用户："+userName;
//        }else{
//            return "系统正在维护中...";
//        }
//    }

    @Operation(summary = "获取系统信息")
    @GetMapping("/getSystemInfo")
    public String getSystemInfo(){
        return "信息："+systemConfig.getName()+"-"+systemConfig.getVersion()+"-"+systemConfig.getAuther();
    }
    /**
     * 获取邮箱验证码
     */
    @Operation(summary = "获取邮箱验证码")
    @GetMapping("/getEmailCode")
    public Result getEmailCode(String email){
        iUserService.getEmailCode(email);
        return Result.success();
    }

    /**
     * 邮箱注册 UserEmailRegisterDto
     */
    @Operation(summary = "邮箱注册")
    @PostMapping("/emailRegister")
    public Result emailRegister(@Valid @RequestBody UserEmailRegisterDto userEmailRegisterDto){
        iUserService.userEmailRegister(userEmailRegisterDto);
        return Result.success();
    }
}
