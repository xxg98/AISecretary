package com.kailei.aisecretary.exception.customize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户名存在异常
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class UsernameExistsException extends RuntimeException {
    private String msg;
}
