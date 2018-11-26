package org.springframework.boot.autoconfigure.orm.jpa;

import java.lang.Class;
import java.lang.Override;
import java.util.Collection;
import javax.sql.DataSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.jdbc.SchemaManagementProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import slim.ConditionService;
import slim.ModuleMapping;

@ModuleMapping(
    module = HibernateJpaAutoConfigurationModule.class
)
public class HibernateJpaConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(HibernateJpaConfiguration.class)) {
      context.registerBean(HibernateProperties.class, () -> new HibernateProperties());
      context.registerBean(HibernateJpaConfiguration.class, () -> new HibernateJpaConfiguration(context.getBean(DataSource.class),context.getBean(JpaProperties.class),context.getBeanFactory(),context.getBeanProvider(JtaTransactionManager.class),context.getBeanProvider(TransactionManagerCustomizers.class),context.getBean(HibernateProperties.class),context.getBeanProvider(ResolvableType.forClassWithGenerics(Collection.class, Collection.class)),context.getBeanProvider(SchemaManagementProvider.class),context.getBeanProvider(PhysicalNamingStrategy.class),context.getBeanProvider(ImplicitNamingStrategy.class),context.getBeanProvider(HibernatePropertiesCustomizer.class)));
      if (conditions.matches(HibernateJpaConfiguration.class, PlatformTransactionManager.class)) {
        context.registerBean("transactionManager", PlatformTransactionManager.class, () -> context.getBean(HibernateJpaConfiguration.class).transactionManager());
      }
      if (conditions.matches(HibernateJpaConfiguration.class, JpaVendorAdapter.class)) {
        context.registerBean("jpaVendorAdapter", JpaVendorAdapter.class, () -> context.getBean(HibernateJpaConfiguration.class).jpaVendorAdapter());
      }
      if (conditions.matches(HibernateJpaConfiguration.class, EntityManagerFactoryBuilder.class)) {
        context.registerBean("entityManagerFactoryBuilder", EntityManagerFactoryBuilder.class, () -> context.getBean(HibernateJpaConfiguration.class).entityManagerFactoryBuilder(context.getBean(JpaVendorAdapter.class),context.getBeanProvider(PersistenceUnitManager.class),context.getBeanProvider(EntityManagerFactoryBuilderCustomizer.class)));
      }
      if (conditions.matches(HibernateJpaConfiguration.class, LocalContainerEntityManagerFactoryBean.class)) {
        context.registerBean("entityManagerFactory", LocalContainerEntityManagerFactoryBean.class, () -> context.getBean(HibernateJpaConfiguration.class).entityManagerFactory(context.getBean(EntityManagerFactoryBuilder.class)));
      }
    }
  }

  public static Class<?> configurations() {
    return HibernateJpaConfiguration.class;
  }
}
