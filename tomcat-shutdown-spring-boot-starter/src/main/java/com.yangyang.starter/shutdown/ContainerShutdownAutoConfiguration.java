package com.yangyang.starter.shutdown;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;

@Configuration
@ConditionalOnClass({ Servlet.class, Tomcat.class })
@Slf4j
public class ContainerShutdownAutoConfiguration {

    @Value("${tomcat.default-timeout:30}")
    private long defaultTimeOut;

    @Bean
    public TomcatGracefulShutdown gracefulShutdown() {
        return new TomcatGracefulShutdown(defaultTimeOut);
    }

    @Bean
    public EmbeddedServletContainerCustomizer tomcatCustomizer(final TomcatGracefulShutdown obj) {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                if (container instanceof TomcatEmbeddedServletContainerFactory) {
                    log.debug("find tomcat container,inject custom shutdown method");
                    ((TomcatEmbeddedServletContainerFactory) container).addConnectorCustomizers(obj);
                }

            }
        };
    }
}
