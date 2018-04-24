package com.yangyang.starter.shutdown;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TomcatGracefulShutdown
        implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent>, Ordered {

    private volatile Connector connector;

    private long timeout;

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    public TomcatGracefulShutdown(long timeout) {
        super();
        this.timeout = timeout;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("TomcatShutdownHook start");
        this.connector.pause();
        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            try {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                threadPoolExecutor.shutdown();
                if (!threadPoolExecutor.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    log.warn("Tomcat thread pool did not shut down gracefully within {} seconds. Proceeding with forceful shutdown", timeout);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("TomcatShutdownHook exit");
    }

    @Override
    public int getOrder() {
        // 最先执行，确保不再接受请求
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
