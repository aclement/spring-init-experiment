package org.springframework.boot.test.autoconfigure.web.servlet;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class MockMvcSecurityAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MockMvcSecurityAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MockMvcSecurityAutoConfiguration.class).length==0) {
        context.getBeanFactory().getBean(ImportRegistrars.class).add(MockMvcSecurityAutoConfiguration.class, "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration");
        context.getBeanFactory().getBean(ImportRegistrars.class).add(MockMvcSecurityAutoConfiguration.class, "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration");
        new MockMvcSecurityConfigurationInitializer().initialize(context);
        context.registerBean(MockMvcSecurityAutoConfiguration.class, () -> new MockMvcSecurityAutoConfiguration());
      }
    }
  }
}
