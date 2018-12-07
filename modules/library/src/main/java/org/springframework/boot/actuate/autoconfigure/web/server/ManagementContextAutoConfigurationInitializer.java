package org.springframework.boot.actuate.autoconfigure.web.server;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ImportRegistrars;

public class ManagementContextAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(ManagementContextAutoConfiguration.class).length==0) {
      new ManagementContextAutoConfiguration_SameManagementContextConfigurationInitializer().initialize(context);
      new ManagementContextAutoConfiguration_DifferentManagementContextConfigurationInitializer().initialize(context);
      context.getBeanFactory().getBean(ImportRegistrars.class).add(ManagementContextAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      new SameManagementContextConfiguration_EnableSameManagementContextConfigurationInitializer().initialize(context);
      context.registerBean(ManagementContextAutoConfiguration.class, () -> new ManagementContextAutoConfiguration());
    }
  }
}
