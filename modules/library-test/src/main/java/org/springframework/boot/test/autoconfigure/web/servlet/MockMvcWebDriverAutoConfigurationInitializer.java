package org.springframework.boot.test.autoconfigure.web.servlet;

import java.lang.Override;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;
import slim.ConditionService;

public class MockMvcWebDriverAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MockMvcWebDriverAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MockMvcWebDriverAutoConfiguration.class).length==0) {
        context.registerBean(MockMvcWebDriverAutoConfiguration.class, () -> new MockMvcWebDriverAutoConfiguration(context.getBean(Environment.class)));
        if (conditions.matches(MockMvcWebDriverAutoConfiguration.class, MockMvcHtmlUnitDriverBuilder.class)) {
          context.registerBean("mockMvcHtmlUnitDriverBuilder", MockMvcHtmlUnitDriverBuilder.class, () -> context.getBean(MockMvcWebDriverAutoConfiguration.class).mockMvcHtmlUnitDriverBuilder(context.getBean(MockMvc.class)));
        }
        if (conditions.matches(MockMvcWebDriverAutoConfiguration.class, HtmlUnitDriver.class)) {
          context.registerBean("htmlUnitDriver", HtmlUnitDriver.class, () -> context.getBean(MockMvcWebDriverAutoConfiguration.class).htmlUnitDriver(context.getBean(MockMvcHtmlUnitDriverBuilder.class)));
        }
      }
    }
  }
}
