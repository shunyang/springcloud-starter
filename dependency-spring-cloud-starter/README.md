## <center>dependency-spring-cloud-starter 

这个starter主要实现了通过添加注解的方式实现上报依赖管理到eureka注册中心

### 使用方法

添加依赖
```
<dependency>
  <groupId>com.yangyang</groupId>
  <artifactId>dependency-spring-cloud-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### 服务依赖上报

在主类上添加注解`@EnableDependenciesReport`即可


### 实现原理：

 服务依赖上报实现原理

通过扫描注解`@FeignClient`，解析得到依赖的服务名称，汇总后写入metadata，例如：
```
"metadata": {
    "sc.service_dependencies": "user-server,pay-server",
    "sc.service_dependencies_detail": 
    "[
       {
         "interfaceName": "com.yangyang.video.account.client.AccountConfigServiceClient",
         "jarName": "video-account-api-1.1.20.jar",
         "serverName": "video-account-service"
       },
       {
         "interfaceName": "com.yangyang.video.client.VideoServiceClient",
         "jarName": "video-client-1.0.10.jar",
         "serverName": "video-server"
       },
       {
         "interfaceName": "com.yangyang.video.account.client.WithdrawServiceClient",
         "jarName": "video-account-api-1.1.20.jar",
         "serverName": "video-account-service"
       }
     ]",
    "sc.service_dependencies.report.enabled": "true"
}
```





