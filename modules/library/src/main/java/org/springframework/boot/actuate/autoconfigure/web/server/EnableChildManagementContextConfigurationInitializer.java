package org.springframework.boot.actuate.autoconfigure.web.server;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class EnableChildManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(EnableChildManagementContextConfiguration.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(EnableChildManagementContextConfiguration.class, "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextConfigurationImportSelector");
      context.registerBean(EnableChildManagementContextConfiguration.class, () -> new EnableChildManagementContextConfiguration());
    }
  }
}
