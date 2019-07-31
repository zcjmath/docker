## 前言
自己想构建一个基于Spring boot的docker项目，主要想验证一套web 应用基于docker持续集成的方案,同时也利用kubernetes
编排自己的容器，做出一个简单的demo。 

> 准备工作
- 需要熟悉简单spring boot 参考官网文档
- 需要熟悉常规mvn，git，linux命令，网上去搜
- 需要一台centos 7机器
- 需要熟悉docker，kubernete 常用命令 官网去搜 


##环境安装

> 安装单机kubernetes

本来想安装集群，可惜自己笔记本内存不够就安装一个单机kubernete,够用就行了哈
参考文档[K8S初体验--单机部署kubernetes](https://blog.csdn.net/wyc_cs/article/details/87623920)

> 遇到问题

- k8s暴露的nodeport,外网访问不了[解决k8s的nodePort，外网不能访问](https://blog.csdn.net/kq1983/article/details/90516052)

- spring boot连接不上docker,提示我们连接被拒绝
```bash
1.进入docker私服所在的服务器,编辑配置文件

vim /usr/lib/systemd/system/docker.service

2.在docker.service的[Service]下增加如下内容:

ExecStart=
ExecStart=/usr/bin/dockerd -H tcp://172.16.146.139:2375 -H unix://var/run/docker.sock

3.重新加载配置文件

systemctl daemon-reload

4.重启docker

systemctl restart docker 

```

- Kubernetes 报错："image pull failed for registry.access.redhat.com/rhel7/pod-infrastructure:latest ..."
```bash
yum install -y *rhsm*
 
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
 
rpm2cpio python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm | cpio -iv --to-stdout ./etc/rhsm/ca/redhat-uep.pem | tee /etc/rhsm/ca/redhat-uep.pem

```


##构建Spring boot工程
可以参考官网，也可以参考[spring Boot 应用通过Docker 来实现构建、运行、发布](https://blog.csdn.net/u010046908/article/details/56008445)
本项目启动时候依赖mysql,而mysql服务通过k8s部署的可以参考[k8s部署mysql](https://www.cnblogs.com/zoulixiang/p/9910337.html)因此需要设置启动参数

> 启动环境变量

- MYSQL_IP=172.16.146.140
容器node节点ip

- MYSQL_PORT=30006
mysql 容器对外暴露的nodePort端口 

> mysql需要建对应库和表

本项目使用数据库zcjdata,表为user

