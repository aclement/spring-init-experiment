package org.springframework.boot.test.autoconfigure.web.servlet;

import java.util.stream.Collectors;

import slim.ConditionService;
import slim.ImportRegistrars;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class MockMvcAutoConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(MockMvcAutoConfiguration.class)) {
			if (context.getBeanFactory()
					.getBeanNamesForType(MockMvcAutoConfiguration.class).length == 0) {
				context.getBeanFactory().getBean(ImportRegistrars.class).add(
						MockMvcAutoConfiguration.class,
						"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
				context.registerBean(MockMvcAutoConfiguration.class,
						() -> new MockMvcAutoConfiguration(
								(WebApplicationContext) context,
								context.getBean(WebMvcProperties.class)));
				if (conditions.matches(MockMvcAutoConfiguration.class,
						DispatcherServletPath.class)) {
					context.registerBean("dispatcherServletPath",
							DispatcherServletPath.class,
							() -> context.getBean(MockMvcAutoConfiguration.class)
									.dispatcherServletPath());
				}
				if (conditions.matches(MockMvcAutoConfiguration.class,
						DefaultMockMvcBuilder.class)) {
					context.registerBean("mockMvcBuilder", DefaultMockMvcBuilder.class,
							() -> context.getBean(MockMvcAutoConfiguration.class)
									.mockMvcBuilder(context
											.getBeanProvider(
													MockMvcBuilderCustomizer.class)
											.stream().collect(Collectors.toList())));
				}
				context.registerBean("springBootMockMvcBuilderCustomizer",
						SpringBootMockMvcBuilderCustomizer.class,
						() -> context.getBean(MockMvcAutoConfiguration.class)
								.springBootMockMvcBuilderCustomizer(),
						def -> {
							def.setFactoryMethodName(
									"springBootMockMvcBuilderCustomizer");
							def.setFactoryBeanName(
									"org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration");
						});
				if (conditions.matches(MockMvcAutoConfiguration.class, MockMvc.class)) {
					context.registerBean("mockMvc", MockMvc.class,
							() -> context.getBean(MockMvcAutoConfiguration.class)
									.mockMvc(context.getBean(MockMvcBuilder.class)));
				}
				if (conditions.matches(MockMvcAutoConfiguration.class,
						DispatcherServlet.class)) {
					context.registerBean("dispatcherServlet", DispatcherServlet.class,
							() -> context.getBean(MockMvcAutoConfiguration.class)
									.dispatcherServlet(context.getBean(MockMvc.class)));
				}
			}
		}
	}
}
