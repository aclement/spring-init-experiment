package org.springframework.boot.autoconfigure.mustache;

import com.samskivert.mustache.Mustache;
import java.lang.Override;
import org.springframework.boot.web.reactive.result.view.MustacheViewResolver;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class MustacheReactiveWebConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MustacheReactiveWebConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MustacheReactiveWebConfiguration.class).length==0) {
        context.registerBean(MustacheReactiveWebConfiguration.class, () -> new MustacheReactiveWebConfiguration(context.getBean(MustacheProperties.class)));
        if (conditions.matches(MustacheReactiveWebConfiguration.class, MustacheViewResolver.class)) {
          context.registerBean("mustacheViewResolver", MustacheViewResolver.class, () -> context.getBean(MustacheReactiveWebConfiguration.class).mustacheViewResolver(context.getBean(Mustache.Compiler.class)));
        }
      }
    }
  }
}
