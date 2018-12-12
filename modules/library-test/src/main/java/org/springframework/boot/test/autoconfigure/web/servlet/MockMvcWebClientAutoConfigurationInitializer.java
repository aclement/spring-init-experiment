package org.springframework.boot.test.autoconfigure.web.servlet;

import com.gargoylesoftware.htmlunit.WebClient;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import slim.ConditionService;

public class MockMvcWebClientAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MockMvcWebClientAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MockMvcWebClientAutoConfiguration.class).length==0) {
        context.registerBean(MockMvcWebClientAutoConfiguration.class, () -> new MockMvcWebClientAutoConfiguration(context.getBean(Environment.class)));
        if (conditions.matches(MockMvcWebClientAutoConfiguration.class, MockMvcWebClientBuilder.class)) {
          context.registerBean("mockMvcWebClientBuilder", MockMvcWebClientBuilder.class, () -> context.getBean(MockMvcWebClientAutoConfiguration.class).mockMvcWebClientBuilder(context.getBean(MockMvc.class)));
        }
        if (conditions.matches(MockMvcWebClientAutoConfiguration.class, WebClient.class)) {
          context.registerBean("htmlUnitWebClient", WebClient.class, () -> context.getBean(MockMvcWebClientAutoConfiguration.class).htmlUnitWebClient(context.getBean(MockMvcWebClientBuilder.class)));
        }
      }
    }
  }
}
