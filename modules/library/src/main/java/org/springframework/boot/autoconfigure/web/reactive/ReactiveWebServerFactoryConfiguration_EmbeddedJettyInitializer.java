package org.springframework.boot.autoconfigure.web.reactive;

import java.lang.Override;
import org.springframework.boot.web.embedded.jetty.JettyReactiveWebServerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.client.reactive.JettyResourceFactory;
import slim.ConditionService;

public class ReactiveWebServerFactoryConfiguration_EmbeddedJettyInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {
	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions
				.matches(ReactiveWebServerFactoryConfiguration.EmbeddedJetty.class)) {
			if (conditions.matches(
					ReactiveWebServerFactoryConfiguration.EmbeddedJetty.class,
					JettyResourceFactory.class)) {
				if (context.getBeanFactory().getBeanNamesForType(
						ReactiveWebServerFactoryConfiguration.EmbeddedJetty.class).length == 0) {
					context.registerBean(
							ReactiveWebServerFactoryConfiguration.EmbeddedJetty.class,
							() -> new ReactiveWebServerFactoryConfiguration.EmbeddedJetty());
				}
				context.registerBean("jettyReactiveWebServerFactory",
						JettyReactiveWebServerFactory.class,
						() -> context.getBean(
								ReactiveWebServerFactoryConfiguration.EmbeddedJetty.class)
								.jettyReactiveWebServerFactory(
										context.getBean(JettyResourceFactory.class)));
				context.registerBean("jettyServerResourceFactory",
						JettyResourceFactory.class,
						() -> context.getBean(
								ReactiveWebServerFactoryConfiguration.EmbeddedJetty.class)
								.jettyServerResourceFactory());
			}
		}
	}
}
