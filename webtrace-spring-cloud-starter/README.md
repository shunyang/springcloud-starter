# Sleuth Web Trace

Sleuth Web Trace可以将Sleuth的TrackId，设置在HTTP Response的Header的`X-J-TraceId`字段中返回给前端。

我们使用了SpringBoot Starter，只要引入以下依赖，并且你的项目依赖了Sleuth，那么Sleuth Web Trace Filter将会自动注入，拦截Response并设置TraceId。

### 集成Sleuth Web Trace

```
<dependency>
	<groupId>cn.yangyang.starter</groupId>
	<artifactId>webtrace-spring-cloud-starter</artifactId>
	<version>1.0.0</version>
</dependency>
```

### 如何发布

如果你修改了该项目，请升级版本号，并执行`mvn deploy`发布到内部的maven仓库


