## 前言
自己想构建一个基于Spring boot的docker项目，主要想验证一套web 应用基于docker持续集成的方案,同时也利用kubernetes
编排自己的容器，做出一个简单的demo。 

> 准备工作
- 需要熟悉简单spring boot 参考官网文档
- 需要熟悉常规mvn，git，linux命令，网上去搜
- 需要一台centos 7机器
- 需要熟悉docker，kubernete 常用命令 官网去搜 


## 环境安装

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


## 构建Spring boot工程
可以参考官网，也可以参考[spring Boot 应用通过Docker 来实现构建、运行、发布](https://blog.csdn.net/u010046908/article/details/56008445)
本项目启动时候依赖mysql,而mysql服务通过k8s部署的可以参考[k8s部署mysql](https://www.cnblogs.com/zoulixiang/p/9910337.html)因此需要设置启动参数

> 启动环境变量

- MYSQL_IP=172.16.146.140
容器node节点ip

- MYSQL_PORT=30006
mysql 容器对外暴露的nodePort端口 

- MYSQL_USERNAME=zhangsan
mysql用户

- MYSQL_PASSWORD=*****
mysql用户密码

> mysql需要建对应库和表

本项目使用数据库zcjdata,表为user


##  打包、发布、运行
1. 编写对应Dockerfile，放在指定目录下

2. 通过 mvn package docker:build 

3. 镜像发布到Docker registry中，通过docker images查看
```text
[root@ai k8s]# docker images
REPOSITORY                                            TAG                 IMAGE ID            CREATED             SIZE
zcjmath/docker                                        1.0.1               50aaad1ee48c        About an hour ago   148 MB
docker.io/mysql                                       latest              2151acc12881        8 days ago          445 MB
docker.io/openjdk                                     8-jdk-alpine        a3562aa0b991        2 months ago        105 MB
registry.access.redhat.com/rhel7/pod-infrastructure   latest              99965fb98423        21 months ago       209 MB
```

如上面zcjmath/docker:1.0.1镜像

4.编写k8s的yaml文件

app-svc.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: app-svc
  labels:
    name: app-svc
spec:
  type: NodePort
  ports:
  - port: 8081
    protocol: TCP
    targetPort: 8081
    name: http
    nodePort: 30081
  selector:
    name: app-pod

```
其中targetPort是应用端口，port是容器端口，nodeport是外部端口

app-rc.yaml
```yaml

apiVersion: v1
kind: ReplicationController
metadata:
  name: app-rc
  labels:
    name: app-rc
spec:
  replicas: 1
  selector:
    name: app-pod
  template:
    metadata:
      labels:
        name: app-pod
    spec:
      containers:
      - name: app
        image: zcjmath/docker:1.0.1
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8081
        env:
        - name: MYSQL_IP
          value: "10.254.111.160"
        - name: MYSQL_PORT
          value: "3306"
        - name: MYSQL_USERNAME
          value: root
        - name: MYSQL_PASSWORD
          value: zcj123


```
image 是本地镜像
env是应用启动需要的环境变量，其中MYSQL_IP必须是k8s分配cluster ip 端口也是容器端口

5. 通过kubectl 启动app应用
```text

kubectl create -f app-svc.yaml 

kubectl create -f app-rc.yaml 
还可以通过 kubectl get svc ,kubectl get pod,docker ps 查看
[root@ai k8s]# kubectl get svc
NAME         CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
app-svc      10.254.30.79     <nodes>       8081:30081/TCP   1h
kubernetes   10.254.0.1       <none>        443/TCP          6h
mysql-svc    10.254.111.160   <nodes>       3306:30006/TCP   2h
[root@ai k8s]# kubectl get pod
NAME             READY     STATUS    RESTARTS   AGE
app-rc-s4rj5     1/1       Running   0          7m
mysql-rc-kwvdw   1/1       Running   0          2h
[root@ai k8s]# docker ps
CONTAINER ID        IMAGE                                                        COMMAND                  CREATED             STATUS              PORTS               NAMES
b976d42cbcd5        zcjmath/docker:1.0.1                                         "java -Djava.secur..."   11 minutes ago      Up 11 minutes                           k8s_app.8afa7577_app-rc-s4rj5_default_8c0644c4-b36f-11e9-ac83-000c2917b091_e2a33036
fa2450d7febe        registry.access.redhat.com/rhel7/pod-infrastructure:latest   "/usr/bin/pod"           11 minutes ago      Up 11 minutes                           k8s_POD.26dc0baa_app-rc-s4rj5_default_8c0644c4-b36f-11e9-ac83-000c2917b091_9c1c5814
c151c41b05cf        docker.io/mysql:latest                                       "docker-entrypoint..."   2 hours ago         Up 2 hours                              k8s_mysql.2ebf24bf_mysql-rc-kwvdw_default_be17349c-b35c-11e9-ac83-000c2917b091_d6f87b8f
be49fbddecc7        registry.access.redhat.com/rhel7/pod-infrastructure:latest   "/usr/bin/pod"           2 hours ago         Up 2 hours                              k8s_POD.1d520ba5_mysql-rc-kwvdw_default_be17349c-b35c-11e9-ac83-000c2917b091_4cd6126d

```


如果pod 没有启动起来可以通过kubectl describe pod app-rc-s4rj5查看pod没有启动原因

如果是pod起来但是应用不能访问，需要查看容器日志可以通过  kubectl logs -f app-rc-s4rj5
也可以通过 docker logs -f b97（容器id可以简写取前3个）

如果想进入容器里面，查看应用的配置文件，可以通过
```text
[root@ai k8s]# docker exec -it b97 /bin/sh
/ # ls
bin    dev    etc    home   lib    media  mnt    opt    proc   root   run    sbin   srv    sys    tmp    usr    var
/ # cd  /opt
/opt # ls
app.jar
/opt # 
```


6.在浏览器使用http://172.16.146.140:30081/user/list访问
返回如下就成功了 
```json
[{"id":1,"name":"zhangsan","age":15},{"id":2,"name":"lisi","age":16},{"id":3,"name":"wangwu","age":16},{"id":4,"name":"lihua","age":16}]
```
