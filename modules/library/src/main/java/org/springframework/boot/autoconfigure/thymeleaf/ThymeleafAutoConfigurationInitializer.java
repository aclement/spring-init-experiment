package org.springframework.boot.autoconfigure.thymeleaf;

import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import slim.ConditionService;
import slim.ImportRegistrars;

public class ThymeleafAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(ThymeleafAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(ThymeleafAutoConfiguration.class).length==0) {
        new ThymeleafAutoConfiguration_DefaultTemplateResolverConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_ThymeleafDefaultConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_ThymeleafWebMvcConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_ThymeleafReactiveConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_ThymeleafWebFluxConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_ThymeleafWebLayoutConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_DataAttributeDialectConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_ThymeleafSecurityDialectConfigurationInitializer().initialize(context);
        new ThymeleafAutoConfiguration_ThymeleafJava8TimeDialectInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(ThymeleafAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        new ThymeleafWebMvcConfiguration_ThymeleafViewResolverConfigurationInitializer().initialize(context);
        context.registerBean(ThymeleafAutoConfiguration.class, () -> new ThymeleafAutoConfiguration());
      }
    }
  }
}
