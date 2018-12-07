package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.util.Collection;
import java.util.stream.Collectors;

import slim.ConditionService;
import slim.ImportRegistrars;

import org.springframework.boot.actuate.autoconfigure.endpoint.ExposeExcludePropertyEndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointsSupplier;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;

public class WebEndpointAutoConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(WebEndpointAutoConfiguration.class)) {
			if (context.getBeanFactory().getBeanNamesForType(
					WebEndpointAutoConfiguration.class).length == 0) {
				new WebEndpointAutoConfiguration_WebEndpointServletConfigurationInitializer()
						.initialize(context);
				context.getBeanFactory().getBean(ImportRegistrars.class).add(
						WebEndpointAutoConfiguration.class,
						"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
				context.registerBean(WebEndpointAutoConfiguration.class,
						() -> new WebEndpointAutoConfiguration(context,
								context.getBean(WebEndpointProperties.class)));
				context.registerBean("webEndpointPathMapper", PathMapper.class,
						() -> context.getBean(WebEndpointAutoConfiguration.class)
								.webEndpointPathMapper());
				if (conditions.matches(WebEndpointAutoConfiguration.class,
						EndpointMediaTypes.class)) {
					context.registerBean("endpointMediaTypes", EndpointMediaTypes.class,
							() -> context.getBean(WebEndpointAutoConfiguration.class)
									.endpointMediaTypes());
				}
				if (conditions.matches(WebEndpointAutoConfiguration.class,
						WebEndpointDiscoverer.class)) {
					context.registerBean("webEndpointDiscoverer",
							WebEndpointDiscoverer.class,
							() -> context.getBean(WebEndpointAutoConfiguration.class)
									.webEndpointDiscoverer(
											context.getBean(ParameterValueMapper.class),
											context.getBean(EndpointMediaTypes.class),
											context.getBeanProvider(PathMapper.class),
											context.getBeanProvider(
													OperationInvokerAdvisor.class),
											context.getBeanProvider(
													ResolvableType.forClassWithGenerics(
															EndpointFilter.class,
															EndpointFilter.class))));
				}
				if (conditions.matches(WebEndpointAutoConfiguration.class,
						ControllerEndpointDiscoverer.class)) {
					context.registerBean("controllerEndpointDiscoverer",
							ControllerEndpointDiscoverer.class,
							() -> context.getBean(WebEndpointAutoConfiguration.class)
									.controllerEndpointDiscoverer(
											context.getBeanProvider(PathMapper.class),
											context.getBeanProvider(
													ResolvableType.forClassWithGenerics(
															Collection.class,
															Collection.class))));
				}
				if (conditions.matches(WebEndpointAutoConfiguration.class,
						PathMappedEndpoints.class)) {
					context.registerBean("pathMappedEndpoints", PathMappedEndpoints.class,
							() -> {
								return context.getBean(WebEndpointAutoConfiguration.class)
										.pathMappedEndpoints(
												generic(context
														.getBeanProvider(
																EndpointsSupplier.class)
														.stream()
														.collect(Collectors.toList())),
												context.getBean(
														WebEndpointProperties.class));
							});
				}
				context.registerBean("webExposeExcludePropertyEndpointFilter",
						ExposeExcludePropertyEndpointFilter.class,
						() -> context.getBean(WebEndpointAutoConfiguration.class)
								.webExposeExcludePropertyEndpointFilter());
				context.registerBean("controllerExposeExcludePropertyEndpointFilter",
						ExposeExcludePropertyEndpointFilter.class,
						() -> context.getBean(WebEndpointAutoConfiguration.class)
								.controllerExposeExcludePropertyEndpointFilter());
			}
		}
	}

	@SuppressWarnings("unchecked")
	// Generics hack...
	<T> T generic(Object thing) {
		return (T) thing;
	}

}
