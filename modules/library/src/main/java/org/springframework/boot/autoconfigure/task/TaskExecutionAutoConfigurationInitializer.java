package org.springframework.boot.autoconfigure.task;

import java.lang.Override;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import slim.ConditionService;
import slim.ImportRegistrars;

public class TaskExecutionAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(TaskExecutionAutoConfiguration.class)) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(TaskExecutionAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      if (context.getBeanFactory().getBeanNamesForType(TaskExecutionAutoConfiguration.class).length==0) {
        context.registerBean(TaskExecutionAutoConfiguration.class, () -> new TaskExecutionAutoConfiguration(context.getBean(TaskExecutionProperties.class),context.getBeanProvider(TaskExecutorCustomizer.class),context.getBeanProvider(TaskDecorator.class)));
      }
      if (conditions.matches(TaskExecutionAutoConfiguration.class, TaskExecutorBuilder.class)) {
        context.registerBean("taskExecutorBuilder", TaskExecutorBuilder.class, () -> context.getBean(TaskExecutionAutoConfiguration.class).taskExecutorBuilder());
      }
      if (conditions.matches(TaskExecutionAutoConfiguration.class, ThreadPoolTaskExecutor.class)) {
        context.registerBean("applicationTaskExecutor", ThreadPoolTaskExecutor.class, () -> context.getBean(TaskExecutionAutoConfiguration.class).applicationTaskExecutor(context.getBean(TaskExecutorBuilder.class)));
      }
    }
  }
}
