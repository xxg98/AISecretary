package com.kailei.aisecretary.exception.customize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class RegistrationException extends RuntimeException{
    private String msg;
}