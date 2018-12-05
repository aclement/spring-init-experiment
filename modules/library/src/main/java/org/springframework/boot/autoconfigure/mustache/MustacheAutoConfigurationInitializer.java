package org.springframework.boot.autoconfigure.mustache;

import com.samskivert.mustache.Mustache;
import java.lang.Override;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import slim.ConditionService;
import slim.ImportRegistrars;

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Update the library
public class MustacheAutoConfigurationInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    ConditionService conditions = context.getBeanFactory().getBean(ConditionService.class);
    if (conditions.matches(MustacheAutoConfiguration.class)) {
      if (context.getBeanFactory().getBeanNamesForType(MustacheAutoConfiguration.class).length==0) {
        new MustacheServletWebConfigurationInitializer().initialize(context);
        new MustacheReactiveWebConfigurationInitializer().initialize(context);
        context.getBeanFactory().getBean(ImportRegistrars.class).add(MustacheAutoConfiguration.class, "org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector");
        context.registerBean(MustacheAutoConfiguration.class, () -> new MustacheAutoConfiguration(context.getBean(MustacheProperties.class),context.getBean(Environment.class),context));
        if (conditions.matches(MustacheAutoConfiguration.class, Mustache.Compiler.class)) {
          context.registerBean("mustacheCompiler", Mustache.Compiler.class, () -> context.getBean(MustacheAutoConfiguration.class).mustacheCompiler(context.getBean(Mustache.TemplateLoader.class)));
        }
        if (conditions.matches(MustacheAutoConfiguration.class, MustacheResourceTemplateLoader.class)) {
          context.registerBean("mustacheTemplateLoader", MustacheResourceTemplateLoader.class, () -> context.getBean(MustacheAutoConfiguration.class).mustacheTemplateLoader());
        }
      }
    }
  }
<<<<<<< HEAD
=======
/**
 * @author Dave Syer
 *
 */
public class MustacheAutoConfigurationInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {

	@Override
	public void initialize(GenericApplicationContext context) {
		ConditionService conditions = context.getBeanFactory()
				.getBean(ConditionService.class);
		if (conditions.matches(MustacheAutoConfiguration.class)) {
			MustacheReactiveWebConfigurationGenerated.initializer().initialize(context);
			context.registerBean(MustacheAutoConfiguration.class);
			if (conditions.matches(MustacheAutoConfiguration.class, Compiler.class)) {
				context.registerBean(Compiler.class,
						() -> context.getBean(MustacheAutoConfiguration.class)
								.mustacheCompiler(context.getBean(TemplateLoader.class)));
			}
			if (conditions.matches(MustacheAutoConfiguration.class,
					TemplateLoader.class)) {
				context.registerBean(TemplateLoader.class,
						() -> context.getBean(MustacheAutoConfiguration.class)
								.mustacheTemplateLoader());
			}
		}
	}

>>>>>>> Add plain JDBC sample (db)
=======
>>>>>>> Update the library
}
