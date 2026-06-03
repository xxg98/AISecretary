package com.kailei.aisecretary.exception;


import cn.dev33.satoken.exception.NotLoginException;
import com.kailei.aisecretary.exception.customize.UserLoginException;
import com.kailei.aisecretary.exception.customize.UsernameExistsException;
import com.kailei.aisecretary.utils.Result;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常拦截器
 */
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    //--------------自定义异常拦截------------------
    //注册
    @ExceptionHandler(UsernameExistsException.class)
    public Result registrationException(UsernameExistsException registrationException){
        return Result.fail(registrationException.getMsg());
    }
    //登录
    @ExceptionHandler(UserLoginException.class)
    public Result userLoginException(UserLoginException userLoginException){
        return Result.fail(userLoginException.getMsg());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result notLoginException(){
        return Result.fail("Token过期或未登录，请先登录");
    }


    //参数校验异常拦截
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<Result<List<String>>> handleValidationException(Exception e) {
        Result<List<String>> result = null;
        if (e instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            result = Result.fail("参数校验异常",
                    methodArgumentNotValidException.getBindingResult().getAllErrors().stream()
                            .filter(FieldError.class::isInstance)
                            .map(error -> ((FieldError) error).getDefaultMessage())
                            .collect(Collectors.toList())).code(400);
        }else if (e instanceof BindException bindException) {
            result = Result.fail( "参数校验异常",
                    bindException.getAllErrors().stream()
                            .filter(FieldError.class::isInstance)
                            .map(error -> ((FieldError) error).getDefaultMessage())
                            .collect(Collectors.toList())).code(400);
        }
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    //运行异常
    @ExceptionHandler(RuntimeException.class)
    public Result runtimeException(RuntimeException runtimeException){
        return Result.fail(runtimeException.getMessage());
    }
}
