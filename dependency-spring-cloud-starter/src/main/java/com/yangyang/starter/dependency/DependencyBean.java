package com.yangyang.starter.dependency;

/**
 * @author chenshunyang
 * @create 2018-05-15 16:49
 **/
public class DependencyBean {
    private String InterfaceName;
    private String jarName;
    private String serverName;

    public String getInterfaceName() {
        return InterfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        InterfaceName = interfaceName;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
