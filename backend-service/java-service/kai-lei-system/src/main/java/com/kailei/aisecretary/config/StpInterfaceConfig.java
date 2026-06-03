package com.kailei.aisecretary.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kailei.aisecretary.entity.RoleEntity;
import com.kailei.aisecretary.entity.UsersAndRoleEntity;
import com.kailei.aisecretary.mapper.IRoleMapper;
import com.kailei.aisecretary.mapper.IUsersAndRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceConfig implements StpInterface {

    @Autowired
    private IRoleMapper iRoleMapper;
    @Autowired
    private IUsersAndRoleMapper iUsersAndRoleMapper;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return null;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        //当前登录的用户的id
        String userId = (String) loginId;
        //先查中间表
        LambdaQueryWrapper<UsersAndRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsersAndRoleEntity::getUserId,userId);
        List<UsersAndRoleEntity> usersAndRoleEntities = iUsersAndRoleMapper.selectList(wrapper);
        //遍历中间表
        //iter //生成foreach循环
        LambdaQueryWrapper<RoleEntity> wrapper1 = new LambdaQueryWrapper<>();
        for (UsersAndRoleEntity usersAndRoleEntity : usersAndRoleEntities) {
            Long roleId = usersAndRoleEntity.getRoleId();
            wrapper1.in(RoleEntity::getId,roleId);
        }
        List<String> roleKeyList = iRoleMapper.selectList(wrapper1).stream()
                // 提取roleKey字段（必须有getRoleKey方法）
                .map(RoleEntity::getRoleKey)
                .collect(Collectors.toList());
        /**
         * List.stream() //启动流式编程
         * .map(对象::get属性名) 提取集合对象的属性
         * .collect(Collectors.toList()); 把提取的属性包装成list进行返回
         */
        return roleKeyList;
    }
}
