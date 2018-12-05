package org.springframework.boot.autoconfigure.mustache;

import com.samskivert.mustache.Mustache;
import java.lang.Override;
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class MustacheServletWebConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MustacheServletWebConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MustacheServletWebConfiguration.class).length==0) {
        context.registerBean(MustacheServletWebConfiguration.class, () -> new MustacheServletWebConfiguration(context.getBean(MustacheProperties.class)));
        if (conditions.matches(MustacheServletWebConfiguration.class, MustacheViewResolver.class)) {
          context.registerBean("mustacheViewResolver", MustacheViewResolver.class, () -> context.getBean(MustacheServletWebConfiguration.class).mustacheViewResolver(context.getBean(Mustache.Compiler.class)));
        }
      }
    }
  }
}
