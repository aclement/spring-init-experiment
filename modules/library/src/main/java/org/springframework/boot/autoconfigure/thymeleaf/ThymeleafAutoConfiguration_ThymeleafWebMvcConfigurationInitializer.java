package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;

public class ThymeleafAutoConfiguration_ThymeleafWebMvcConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafWebMvcConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.ThymeleafWebMvcConfiguration.class).length==0) {
        new ThymeleafWebMvcConfiguration_ThymeleafViewResolverConfigurationInitializer().initialize(context);
        context.registerBean(ThymeleafAutoConfiguration.ThymeleafWebMvcConfiguration.class, () -> new ThymeleafAutoConfiguration.ThymeleafWebMvcConfiguration());
        if (conditions.matches(ThymeleafAutoConfiguration.ThymeleafWebMvcConfiguration.class, FilterRegistrationBean.class)) {
          context.registerBean("resourceUrlEncodingFilter", FilterRegistrationBean.class, () -> context.getBean(ThymeleafAutoConfiguration.ThymeleafWebMvcConfiguration.class).resourceUrlEncodingFilter());
        }
      }
    }
  }
}
