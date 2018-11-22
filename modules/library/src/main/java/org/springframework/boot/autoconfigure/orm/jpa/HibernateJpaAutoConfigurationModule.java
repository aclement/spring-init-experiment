package org.springframework.boot.autoconfigure.orm.jpa;

import java.lang.Override;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import slim.Module;

@Import(HibernateJpaConfiguration.class)
public class HibernateJpaAutoConfigurationModule implements Module {
  @Override
  public List<ApplicationContextInitializer<GenericApplicationContext>> initializers() {
    return Arrays.asList(new HibernateJpaConfigurationInitializer(), new HibernateJpaAutoConfigurationInitializer());
  }
}
