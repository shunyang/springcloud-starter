package com.yangyang.starter.dependency;


import java.util.HashSet;
import java.util.Set;

public class ServiceDependency {

    private Set<String> dependingApps = new HashSet<>();
    private String dependencyDetails;

    public Set<String> getDependingApps() {
        return dependingApps;
    }

    public void setDependingApps(Set<String> dependingApps) {
        this.dependingApps = dependingApps;
    }

    public String getDependencyDetails() {
        return dependencyDetails;
    }

    public void setDependencyDetails(String dependencyDetails) {
        this.dependencyDetails = dependencyDetails;
    }
}


