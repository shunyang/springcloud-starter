package com.yangyang.starter.dependency;

import com.google.common.base.Joiner;
import com.netflix.appinfo.ApplicationInfoManager;

import java.util.Map;

public class DependenciesReporter {

    private ServiceDependency serviceDependency;
    private ApplicationInfoManager applicationInfoManager;

    public DependenciesReporter(ServiceDependency serviceDependency, ApplicationInfoManager applicationInfoManager) {

        this.serviceDependency = serviceDependency;
        this.applicationInfoManager = applicationInfoManager;

        updateMetadata();
    }

    public void updateMetadata(){
        Map<String, String> metadata = this.applicationInfoManager.getInfo().getMetadata();
        metadata.put("sc.service_dependencies", Joiner.on(",").join(this.serviceDependency.getDependingApps()));
        metadata.put("sc.service_dependencies_detail", this.serviceDependency.getDependencyDetails());
        metadata.put("sc.service_dependencies.report.enabled", "true");
    }
}
