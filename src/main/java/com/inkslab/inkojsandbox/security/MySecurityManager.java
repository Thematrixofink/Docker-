package com.inkslab.inkojsandbox.security;

import java.security.Permission;

public class MySecurityManager extends SecurityManager{
    //检查所有权限
    @Override
    public void checkPermission(Permission perm) {
        super.checkPermission(perm);
    }

    //检查程序是否可执行文件
    @Override
    public void checkExec(String cmd) {
        super.checkExec(cmd);
    }

    //检查程序是否可以读文件
    @Override
    public void checkRead(String file) {
        super.checkRead(file);
    }
    //检查程序是否可以写文件
    @Override
    public void checkWrite(String file) {
        super.checkWrite(file);
    }

    @Override
    public void checkDelete(String file) {
        super.checkDelete(file);
    }

    @Override
    public void checkConnect(String host, int port) {
        super.checkConnect(host, port);
    }
}
