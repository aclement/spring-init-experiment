package org.springframework.boot.autoconfigure.task;

import java.lang.Override;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import slim.ConditionService;
import slim.ImportRegistrars;

public class TaskSchedulingAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(TaskSchedulingAutoConfiguration.class)) {
      context.getBeanFactory().getBean(ImportRegistrars.class).add(TaskSchedulingAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
      if (context.getBeanFactory().getBeanNamesForType(TaskSchedulingAutoConfiguration.class).length==0) {
        context.registerBean(TaskSchedulingAutoConfiguration.class, () -> new TaskSchedulingAutoConfiguration());
      }
      if (conditions.matches(TaskSchedulingAutoConfiguration.class, ThreadPoolTaskScheduler.class)) {
        context.registerBean("taskScheduler", ThreadPoolTaskScheduler.class, () -> context.getBean(TaskSchedulingAutoConfiguration.class).taskScheduler(context.getBean(TaskSchedulerBuilder.class)));
      }
      if (conditions.matches(TaskSchedulingAutoConfiguration.class, TaskSchedulerBuilder.class)) {
        context.registerBean("taskSchedulerBuilder", TaskSchedulerBuilder.class, () -> context.getBean(TaskSchedulingAutoConfiguration.class).taskSchedulerBuilder(context.getBean(TaskSchedulingProperties.class),context.getBeanProvider(TaskSchedulerCustomizer.class)));
      }
    }
  }
}
