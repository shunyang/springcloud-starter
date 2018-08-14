/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yangyang.starter.dependency;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

/**
 * @author Spencer Gibb
 * @author Jakub Narloch
 * @author Venil Noronha
 */
public class FeignClientsScanner implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware{

	// patterned after Spring Integration IntegrationComponentScanRegistrar
	// and RibbonClientsConfigurationRegistgrar

	private ResourceLoader resourceLoader;

	private ClassLoader classLoader;

	private Environment environment;

	Set<String> dependingApps = new HashSet<>();

	private List<DependencyBean> dependencyBeans = new ArrayList<>();

	public FeignClientsScanner() {
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata,
										BeanDefinitionRegistry registry) {

		//扫描FeignClient，将clientName添加到dependingApps
		if(metadata.hasAnnotation("org.springframework.cloud.netflix.feign.EnableFeignClients")){
			scanFeignClients(metadata, EnableFeignClients.class, FeignClient.class);
		}

		if(!this.dependingApps.isEmpty()){
			//注册bean： ServiceDependency
			registerDependency(registry);
		}
	}

	public void scanFeignClients(AnnotationMetadata metadata, Class enableFeignClientsClass, Class feignClientClass) {
		ClassPathScanningCandidateComponentProvider scanner = getScanner();
		scanner.setResourceLoader(this.resourceLoader);

		Set<String> basePackages;

		Map<String, Object> attrs = metadata.getAnnotationAttributes(enableFeignClientsClass.getName());
		AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(feignClientClass);
		final Class<?>[] clients = attrs == null ? null : (Class<?>[]) attrs.get("clients");
		if (clients == null || clients.length == 0) {
			scanner.addIncludeFilter(annotationTypeFilter);
			basePackages = getBasePackages(metadata, enableFeignClientsClass);
		}
		else {
			final Set<String> clientClasses = new HashSet<>();
			basePackages = new HashSet<>();
			for (Class<?> clazz : clients) {
				basePackages.add(ClassUtils.getPackageName(clazz));
				clientClasses.add(clazz.getCanonicalName());
			}
			AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
				@Override
				protected boolean match(ClassMetadata metadata) {
					String cleaned = metadata.getClassName().replaceAll("\\$", ".");
					return clientClasses.contains(cleaned);
				}
			};
			scanner.addIncludeFilter(new AllTypeFilter(Arrays.asList(filter, annotationTypeFilter)));
		}
		for (String basePackage : basePackages) {
			Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
			for (BeanDefinition candidateComponent : candidateComponents) {
				if (candidateComponent instanceof AnnotatedBeanDefinition) {
					// verify annotated class is an interface
					AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
					AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
					Assert.isTrue(annotationMetadata.isInterface(),
							"@FeignClient can only be specified on an interface");
					Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(feignClientClass.getCanonicalName());
					String name = getClientName(attributes, feignClientClass);
					String[] interfaceNames = annotationMetadata.getInterfaceNames();
					if (interfaceNames.length == 0){//没有继承任何接口
						DependencyBean dependencyBean = makeDependencyBean(name,annotationMetadata.getClassName());
						dependencyBeans.add(dependencyBean);
					}else{
						for (String interfaceName : interfaceNames){
							DependencyBean dependencyBean = makeDependencyBean(name,interfaceName);
							dependencyBeans.add(dependencyBean);
						}
					}
					dependingApps.add(name);
				}
			}
		}
	}

	private DependencyBean makeDependencyBean(String serverName,String interfaceName) {
		DependencyBean dependencyBean = new DependencyBean();
		dependencyBean.setInterfaceName(interfaceName);
		Class<?> clz = null;
		try {
            clz = Class.forName(interfaceName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
		dependencyBean.setJarName(getJarName(clz));
		dependencyBean.setServerName(serverName);
		return dependencyBean;
	}

	private void registerDependency(BeanDefinitionRegistry registry) {
		String className = "jg#dependency";
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(ServiceDependency.class);
		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		beanDefinition.getPropertyValues().add("dependingApps", dependingApps);
        beanDefinition.getPropertyValues().add("dependencyDetails", JSON.toJSONString(dependencyBeans));
		registry.registerBeanDefinition(className, beanDefinition);
	}

	protected ClassPathScanningCandidateComponentProvider getScanner() {
		return new ClassPathScanningCandidateComponentProvider(false, this.environment) {

			@Override
			protected boolean isCandidateComponent(
					AnnotatedBeanDefinition beanDefinition) {
				if (beanDefinition.getMetadata().isIndependent()) {
					// TODO until SPR-11711 will be resolved
					if (beanDefinition.getMetadata().isInterface()
							&& beanDefinition.getMetadata()
							.getInterfaceNames().length == 1
							&& Annotation.class.getName().equals(beanDefinition
							.getMetadata().getInterfaceNames()[0])) {
						try {
							Class<?> target = ClassUtils.forName(
									beanDefinition.getMetadata().getClassName(),
									FeignClientsScanner.this.classLoader);
							return !target.isAnnotation();
						}
						catch (Exception ex) {
							this.logger.error("Could not load target class: " + beanDefinition.getMetadata().getClassName(), ex);
						}
					}
					return true;
				}
				return false;

			}
		};
	}

	protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata, Class enableFeignClientsClass) {
		Map<String, Object> attributes = importingClassMetadata
				.getAnnotationAttributes(enableFeignClientsClass.getCanonicalName());

		Set<String> basePackages = new HashSet<>();
		for (String pkg : (String[]) attributes.get("value")) {
			if (StringUtils.hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (String pkg : (String[]) attributes.get("basePackages")) {
			if (StringUtils.hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}

		if (basePackages.isEmpty()) {
			basePackages.add(
					ClassUtils.getPackageName(importingClassMetadata.getClassName()));
		}
		return basePackages;
	}

	private String getClientName(Map<String, Object> client, Class feignClientClass) {
		if (client == null) {
			return null;
		}
		String value = (String) client.get("value");
		if (!StringUtils.hasText(value)) {
			value = (String) client.get("name");
		}
		if (!StringUtils.hasText(value)) {
			value = (String) client.get("serviceId");
		}
		if (StringUtils.hasText(value)) {
			return value;
		}

		throw new IllegalStateException("Either 'name' or 'value' must be provided in @"
				+ feignClientClass.getSimpleName());
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Helper class to create a {@link TypeFilter} that matches if all the delegates
	 * match.
	 *
	 * @author Oliver Gierke
	 */
	private static class AllTypeFilter implements TypeFilter {

		private final List<TypeFilter> delegates;

		/**
		 * Creates a new {@link AllTypeFilter} to match if all the given delegates match.
		 *
		 * @param delegates must not be {@literal null}.
		 */
		public AllTypeFilter(List<TypeFilter> delegates) {

			Assert.notNull(delegates);
			this.delegates = delegates;
		}

		@Override
		public boolean match(MetadataReader metadataReader,
							 MetadataReaderFactory metadataReaderFactory) throws IOException {

			for (TypeFilter filter : this.delegates) {
				if (!filter.match(metadataReader, metadataReaderFactory)) {
					return false;
				}
			}

			return true;
		}
	}

	private String getJarName(Class<?> clz) {
		URL loc = clz.getProtectionDomain().getCodeSource().getLocation();
		String protocol = loc.getProtocol();
		String absolutePath = loc.getPath();
		//log.debug("class:{}，绝对路径:{}", clz, absolutePath);
		if ("jar".equals(protocol)) {
			// jar格式的路径样式：file:/C:/Users/Administrator/Documents/iportal-devservice/target/iportal-devservice.jar!/BOOT-INF/lib/sms-pay-api-1.1.0.jar!/
			absolutePath = absolutePath.substring(0, absolutePath.length() - 2);// 切割尾部的'!/'
		}
		// 获取最后一级的文件名
		String path = absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
		//log.debug("class:{}，获取所属包名为:{}", clz, path);
		return path;
	}
}
