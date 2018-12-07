package org.springframework.boot.actuate.autoconfigure.info;

import java.lang.Override;
import org.springframework.boot.actuate.info.EnvironmentInfoContributor;
import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import slim.ConditionService;
import slim.ImportRegistrars;

public class InfoContributorAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    if (context.getBeanFactory().getBeanNamesForType(InfoContributorAutoConfiguration.class).length==0) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(InfoContributorAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      context.registerBean(InfoContributorAutoConfiguration.class, () -> new InfoContributorAutoConfiguration(context.getBean(InfoContributorProperties.class)));
      ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
      if (conditions.matches(InfoContributorAutoConfiguration.class, EnvironmentInfoContributor.class)) {
        context.registerBean("envInfoContributor", EnvironmentInfoContributor.class, () -> context.getBean(InfoContributorAutoConfiguration.class).envInfoContributor(context.getBean(ConfigurableEnvironment.class)));
      }
      if (conditions.matches(InfoContributorAutoConfiguration.class, GitInfoContributor.class)) {
        context.registerBean("gitInfoContributor", GitInfoContributor.class, () -> context.getBean(InfoContributorAutoConfiguration.class).gitInfoContributor(context.getBean(GitProperties.class)));
      }
      if (conditions.matches(InfoContributorAutoConfiguration.class, InfoContributor.class)) {
        context.registerBean("buildInfoContributor", InfoContributor.class, () -> context.getBean(InfoContributorAutoConfiguration.class).buildInfoContributor(context.getBean(BuildProperties.class)));
      }
    }
  }
}
