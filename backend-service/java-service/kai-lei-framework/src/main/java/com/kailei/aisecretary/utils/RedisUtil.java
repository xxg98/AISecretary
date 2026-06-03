package com.kailei.aisecretary.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis通用工具类
 * 适配：JDK21 + SpringBoot4 最新API
 * 修复点：弃用TimeUnit，全量使用Duration时间类型，解决long类型不兼容报错
 * 封装String/Hash/List/ZSet常用操作，可直接业务层注入使用
 *
 * @author 开发人员
 * @date 2026
 */
@Component
public class RedisUtil {

    /**
     * 注入全局配置好的RedisTemplate（序列化已统一配置）
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ================================ String字符串类型操作 ================================

    /**
     * 存入永久String键值对
     * @param key Redis键
     * @param value Redis值（支持对象自动JSON序列化）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存入带过期时间的String键值对
     * @param key Redis键
     * @param value Redis值（支持对象）
     * @param seconds 过期时间（单位：秒）
     */
    public void set(String key, Object value, long seconds) {
        // SpringBoot4强制使用Duration，替代旧版long+TimeUnit
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    /**
     * 根据Key获取String类型值
     * @param key Redis键
     * @return 存储的值（自动反序列化）
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // ================================ Hash哈希类型操作 ================================

    /**
     * 向Hash中存入单个字段键值
     * @param key Hash外层大Key
     * @param hashKey Hash内部小字段Key
     * @param value Hash内部字段值
     */
    public void hPut(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 获取Hash中单个字段的值
     * @param key Hash外层大Key
     * @param hashKey Hash内部小字段Key
     * @return 对应字段的值
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 获取整个Hash的所有键值对
     * @param key Hash外层大Key
     * @return 全量Hash集合Map
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // ================================ List列表类型操作 ================================

    /**
     * 向左往List集合插入元素（队列头）
     * @param key List集合Key
     * @param value 插入元素
     */
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 按索引范围获取List集合元素
     * @param key List集合Key
     * @param start 起始索引
     * @param end 结束索引（-1代表全部）
     * @return 区间内元素集合
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ================================ ZSet有序集合（排行榜）操作 ================================

    /**
     * 向有序集合添加元素，附带分数排序
     * @param key ZSet集合Key
     * @param value 存入元素
     * @param score 排序分数
     */
    public void zAdd(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 根据分数倒序查询ZSet（常用于排行榜）
     * @param key ZSet集合Key
     * @param start 起始下标
     * @param end 结束下标
     * @return 倒序元素集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 累加ZSet中元素的分数
     * @param key ZSet集合Key
     * @param value 目标元素
     * @param score 要增加的分数
     * @return 累加后的最新分数
     */
    public Double zIncrBy(String key, Object value, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    // ================================ 公共通用操作 ================================

    /**
     * 删除指定Key（支持任意类型）
     * @param key 要删除的Redis键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 给已存在的Key设置过期时间
     * @param key Redis键
     * @param seconds 过期秒数
     * @return 是否设置成功
     */
    public Boolean expire(String key, long seconds) {
        return redisTemplate.expire(key, Duration.ofSeconds(seconds));
    }

    /**
     * 判断指定Key是否存在
     * @param key Redis键
     * @return true-存在 false-不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}