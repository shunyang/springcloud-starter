package com.yangyang.starter.dependency;

import com.netflix.appinfo.ApplicationInfoManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ServiceDependency.class)
public class DependenciesReportConfiguration {

    @Bean
    public DependenciesReporter jgDiscoveryClient(ServiceDependency serviceDependency, ApplicationInfoManager applicationInfoManager){
        return new DependenciesReporter(serviceDependency, applicationInfoManager);
    }
}
