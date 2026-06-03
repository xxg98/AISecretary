package com.kailei.aisecretary.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    //不能修改不能删除 List.of
    private List knife4j = List.of(
            "/doc.html",
            "/webjars/css/**",
            "/webjars/js/**",
            "/v3/api-docs/**"
    );

    private List login = List.of(
            "/api/v1/user/passRegister", //用户注册
            "/api/v1/user/userLogin", //用户登录
            "/api/v1/user/getEmailCode", //获取邮箱验证码
            "/api/v1/user/emailRegister" //邮箱注册
    );

    private List banner = List.of(
            "/api/v1/slide/getBanners"
    );




    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(knife4j)
                .excludePathPatterns(banner)
                .excludePathPatterns(login);
    }
}
