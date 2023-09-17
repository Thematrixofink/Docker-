package com.inkslab.inkojsandbox.security;

import java.security.Permission;

/**
 * 安全管理器
 */
public class DefaultSecurityManager extends SecurityManager{
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何权限控制");
        super.checkPermission(perm);
        throw new SecurityException("权限不足" + perm.getActions());
    }
}
