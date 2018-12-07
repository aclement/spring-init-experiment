package org.springframework.boot.actuate.autoconfigure.web.server;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class SameManagementContextConfiguration_EnableSameManagementContextConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(ManagementContextAutoConfiguration.SameManagementContextConfiguration.EnableSameManagementContextConfiguration.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(ManagementContextAutoConfiguration.SameManagementContextConfiguration.EnableSameManagementContextConfiguration.class, "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextConfigurationImportSelector");
      context.registerBean(ManagementContextAutoConfiguration.SameManagementContextConfiguration.EnableSameManagementContextConfiguration.class, () -> new ManagementContextAutoConfiguration.SameManagementContextConfiguration.EnableSameManagementContextConfiguration());
    }
  }
}
