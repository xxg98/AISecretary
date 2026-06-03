package com.kailei.aisecretary.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kailei.aisecretary.dto.UserEmailRegisterDto;
import com.kailei.aisecretary.exception.customize.UserLoginException;
import com.kailei.aisecretary.exception.customize.UsernameExistsException;
import com.kailei.aisecretary.mapper.IUserMapper;
import com.kailei.aisecretary.entity.UsersEntity;
import com.kailei.aisecretary.service.IUserService;
import com.kailei.aisecretary.utils.RedisUtil;
import com.kailei.aisecretary.vo.UsersLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserMapper iUserMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public UsersEntity selectUserById(Long id) {
        return iUserMapper.selectById(id);
    }

    @Override
    public boolean userRegister(UsersEntity usersEntity) {
        //用户名是否重复
        LambdaQueryWrapper<UsersEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsersEntity::getName, usersEntity.getName());
        UsersEntity selectEntity = iUserMapper.selectOne(wrapper);
        if(selectEntity != null){
            throw new UsernameExistsException("用户名已存在");
        }

        //昵称是否重复
        LambdaQueryWrapper<UsersEntity> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(UsersEntity::getNickname, usersEntity.getNickname());
        UsersEntity selectEntity1 = iUserMapper.selectOne(wrapper1);
        if(selectEntity1 != null){
            throw new UsernameExistsException("昵称已存在");
        }

        //加密工具类
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        //盐值 = 随机字符串
        String saltValue = RandomUtil.randomString(10);
        //md5 摘要加密 密码+盐值 一起加密
        String digestHex = md5.digestHex(usersEntity.getPass()+saltValue);
        //将盐值和加密密码存入数据库
        usersEntity.setPass(digestHex);
        usersEntity.setSaltValue(saltValue);

        iUserMapper.insert(usersEntity);
        return true;
    }

    @Override
    public UsersLoginVo userLogin(UsersEntity usersEntity) {
        //通过用户名查询用户信息
        LambdaQueryWrapper<UsersEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsersEntity::getName, usersEntity.getName());
        UsersEntity selectEntity = iUserMapper.selectOne(wrapper);
        if(selectEntity == null){
            throw new UserLoginException("用户名或密码错误");
        }
        //获取盐值
        String saltValue = selectEntity.getSaltValue();
        //登录用的密码
        String loginPass = usersEntity.getPass();
        //加密工具类
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        //md5 摘要加密 密码+固定盐值 一起加密
        String digestHex = md5.digestHex(loginPass+saltValue);

        //获取数据库的加密后的密码
        String pass = selectEntity.getPass();
        // 加密后的密码 != 数据库的密码
        if(!digestHex.equals(pass)){
            throw new UserLoginException("用户名或密码错误");
        }

        //登录成功  就把登录的用户的id给权限框架
        // 加工token  id+过期时间+配置信息.....
        StpUtil.login(selectEntity.getId());
        // 获取加工结果
        String tokenValue = StpUtil.getTokenValue();

        String nickname = selectEntity.getNickname();
        UsersLoginVo usersLoginVo = new UsersLoginVo();
        usersLoginVo.setToken(tokenValue);
        usersLoginVo.setNickname(nickname);

        return usersLoginVo;
    }

    @Override
    public String getEmailCode(String email) {
        // 检查1分钟内是否已经发送过验证码
        String lastSendKey = "email_code:last_send:" + email;
        if (redisUtil.hasKey(lastSendKey)) {
            throw new RuntimeException("请勿频繁发送验证码，请1分钟后再试");
        }

        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 存储验证码到Redis，设置5分钟过期
        String codeKey = "email_code:verify:" + email;
        redisUtil.set(codeKey, code, 300); // 300秒 = 5分钟

        // 记录发送时间，设置1分钟过期
        redisUtil.set(lastSendKey, System.currentTimeMillis(), 60); // 60秒 = 1分钟

        MailUtil.send(email, "斗地主-邮箱验证码", "您的验证码是：" + code + "，有效期5分钟", false);

        // 开发环境下可以返回验证码用于测试
        return code;
    }

    @Override
    public boolean userEmailRegister(UserEmailRegisterDto userEmailRegisterDto) {
        // 邮箱注册
        LambdaQueryWrapper<UsersEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsersEntity::getEmail, userEmailRegisterDto.getEmail());
        UsersEntity selectEntity = iUserMapper.selectOne(wrapper);
        if(selectEntity != null){
            throw new UsernameExistsException("邮箱已存在");
        }
        // 验证码是否有效
        String codeKey = "email_code:verify:" + userEmailRegisterDto.getEmail();
        String code = (String) redisUtil.get(codeKey);
        if(code == null || !code.equals(userEmailRegisterDto.getCode())){
            throw new RuntimeException("验证码错误或已过期");
        }
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setEmail(userEmailRegisterDto.getEmail());
        //直接注册
        iUserMapper.insert(usersEntity);
        return true;
    }
}
