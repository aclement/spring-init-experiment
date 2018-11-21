package lib.registrar.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

class SampleRegistrarWithAwareness implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware, BeanFactoryAware,
		EnvironmentAware, ResourceLoaderAware {

	private Environment environment;
	private BeanFactory beanFactory;
	private ClassLoader classLoader;
	private ResourceLoader resourceLoader;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		StringBuilder value = new StringBuilder();
		value.append("environment="+(environment==null?"null":"notnull"));
		value.append(" beanFactory="+(beanFactory==null?"null":"notnull"));
		value.append(" classLoader="+(classLoader==null?"null":"notnull"));
		value.append(" resourceLoader="+(resourceLoader==null?"null":"notnull"));

		GenericBeanDefinition gbd = new GenericBeanDefinition();
		gbd.setBeanClass(Foo.class);
		gbd.getPropertyValues().addPropertyValue("value", value.toString());
		registry.registerBeanDefinition("foo", gbd);
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
