package com.yangyang.starter.dependency;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(FeignClientsScanner.class)
public @interface EnableDependenciesReport {}
