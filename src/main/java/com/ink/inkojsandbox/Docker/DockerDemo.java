package com.ink.inkojsandbox.Docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;

public class DockerDemo {
    public static void main(String[] args) throws InterruptedException {
        //获取默认的Docker client实例
        //此处是建造者模式
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "nginx:latest";
        //拉取镜像
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);

    }
}
