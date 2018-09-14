package boot.autoconfigure.web.reactive;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.EnableWebFluxConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration.WebFluxConfig;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxRegistrations;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.support.HandlerFunctionAdapter;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.function.server.support.ServerResponseResultHandler;
import org.springframework.web.reactive.result.SimpleHandlerAdapter;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
import org.springframework.web.reactive.result.view.ViewResolutionResultHandler;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.i18n.LocaleContextResolver;

import slim.ObjectProviders;
import slim.SlimConfiguration;

@SlimConfiguration
class WebFluxAutoConfigurationGenerated {
	
	public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
		return new Initializer();
	}
	
	static class Initializer implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			context.registerBean(EnableWebFluxConfigurationWrapper.class,
					() -> new EnableWebFluxConfigurationWrapper(context));
			context.registerBean(HandlerFunctionAdapter.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.handlerFunctionAdapter());
			context.registerBean(WebHttpHandlerBuilder.LOCALE_CONTEXT_RESOLVER_BEAN_NAME,
					LocaleContextResolver.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.localeContextResolver());
			context.registerBean(RequestMappingHandlerAdapter.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.requestMappingHandlerAdapter());
			context.registerBean(RequestMappingHandlerMapping.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.requestMappingHandlerMapping());
			context.registerBean(HandlerMapping.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.resourceHandlerMapping());
			context.registerBean(ResponseBodyResultHandler.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.responseBodyResultHandler());
			context.registerBean(ResponseEntityResultHandler.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.responseEntityResultHandler());
			context.registerBean(WebExceptionHandler.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.responseStatusExceptionHandler());
			context.registerBean(RouterFunctionMapping.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.routerFunctionMapping());
			context.registerBean(WebHttpHandlerBuilder.SERVER_CODEC_CONFIGURER_BEAN_NAME,
					ServerCodecConfigurer.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.serverCodecConfigurer());
			context.registerBean(ServerResponseResultHandler.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.serverResponseResultHandler());
			context.registerBean(SimpleHandlerAdapter.class, () -> context
					.getBean(EnableWebFluxConfigurationWrapper.class).simpleHandlerAdapter());
			context.registerBean(ViewResolutionResultHandler.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.viewResolutionResultHandler());
			context.registerBean(ReactiveAdapterRegistry.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.webFluxAdapterRegistry());
			context.registerBean(RequestedContentTypeResolver.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.webFluxContentTypeResolver());
			context.registerBean(FormattingConversionService.class,
					() -> context.getBean(EnableWebFluxConfigurationWrapper.class)
							.webFluxConversionService());
			context.registerBean(Validator.class, () -> context
					.getBean(EnableWebFluxConfigurationWrapper.class).webFluxValidator());
			context.registerBean(WebHttpHandlerBuilder.WEB_HANDLER_BEAN_NAME,
					DispatcherHandler.class, () -> context
							.getBean(EnableWebFluxConfigurationWrapper.class).webHandler());
			context.registerBean(WebFluxConfigurer.class,
					() -> new WebFluxConfig(context.getBean(ResourceProperties.class),
							context.getBean(WebFluxProperties.class), context,
							ObjectProviders.provider(context, WebFluxConfig.class, 3),
							ObjectProviders.provider(context, WebFluxConfig.class, 4),
							// TODO: still need ObjectProviders for this (private class in
							// public constructor):
							ObjectProviders.provider(context, WebFluxConfig.class, 5),
							ObjectProviders.provider(context, WebFluxConfig.class, 6)));
		}
		
	}

}

class EnableWebFluxConfigurationWrapper extends EnableWebFluxConfiguration {

	public EnableWebFluxConfigurationWrapper(GenericApplicationContext context) {
		super(context.getBean(WebFluxProperties.class),
				context.getBeanProvider(WebFluxRegistrations.class));
	}

}