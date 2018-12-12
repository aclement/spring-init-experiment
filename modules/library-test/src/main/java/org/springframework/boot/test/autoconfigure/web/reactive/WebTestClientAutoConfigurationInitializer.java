package org.springframework.boot.test.autoconfigure.web.reactive;

import java.lang.Override;
import java.util.stream.Collectors;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.web.reactive.server.MockServerConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import slim.ConditionService;
import slim.ImportRegistrars;

public class WebTestClientAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(WebTestClientAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(WebTestClientAutoConfiguration.class).length==0) {
        new WebTestClientSecurityConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(WebTestClientAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(WebTestClientAutoConfiguration.class, () -> new WebTestClientAutoConfiguration());
        if (conditions.matches(WebTestClientAutoConfiguration.class, WebTestClient.class)) {
          context.registerBean("webTestClient", WebTestClient.class, () -> context.getBean(WebTestClientAutoConfiguration.class).webTestClient(context,context.getBeanProvider(WebTestClientBuilderCustomizer.class).stream().collect(Collectors.toList()),context.getBeanProvider(MockServerConfigurer.class).stream().collect(Collectors.toList())));
        }
        context.registerBean("springBootWebTestClientBuilderCustomizer", SpringBootWebTestClientBuilderCustomizer.class, () -> context.getBean(WebTestClientAutoConfiguration.class).springBootWebTestClientBuilderCustomizer(context.getBeanProvider(CodecCustomizer.class)), def -> { def.setFactoryMethodName("springBootWebTestClientBuilderCustomizer"); def.setFactoryBeanName("org.springframework.boot.test.autoconfigure.web.reactive.WebTestClientAutoConfiguration");});
      }
    }
  }
}
