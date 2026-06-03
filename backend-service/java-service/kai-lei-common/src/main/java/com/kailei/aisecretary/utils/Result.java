package com.kailei.aisecretary.utils;

import lombok.Data;

/**
 * 全局统一返回对象（JDK 21 适配版 | 泛型 + 链式调用）
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // 私有构造方法（仅内部使用）
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ====================== 链式调用方法 ======================
    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }

    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    // ====================== 快捷构建方法（核心：移除private build()） ======================
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 失败响应（默认状态码+提示）
     */
    public static <T> Result<T> fail() {
        return new Result<>(500, "操作失败", null);
    }

    /**
     * 失败响应（自定义提示）
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码+提示）
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（自定义状态码+参数）
     */
    public static <T> Result<T> fail(String message,T data) {
        return new Result<>(500, message, data);
    }

    /**
     * 空构造（备用，防止Lombok/JDK编译识别问题）
     */
    private Result() {}
}