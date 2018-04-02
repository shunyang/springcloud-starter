package com.yangyang.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenshunyang
 * @create 2018-04-02 16:35
 **/
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({Tracer.class})
public class SleuthWebTrackAutoConfigure {


    @Bean
    public SleuthWebTraceFilter getSleuthWebTraceFilter(Tracer tracer) {
        return new SleuthWebTraceFilter(tracer);
    }
}
