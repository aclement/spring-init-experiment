package slim;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.StandardAnnotationMetadata;

/**
 * @author Andy Clement
 */
public class CollectorService {

	private GenericApplicationContext context;

	private Map<Class<? extends ImportBeanDefinitionRegistrar>, Supplier<? extends ImportBeanDefinitionRegistrar>> registrars = new LinkedHashMap<>();

	public CollectorService(GenericApplicationContext context) {
		this.context = context;
	}
	
	public <T extends ImportBeanDefinitionRegistrar> void registerRegistrar(Class<T> clazz, Supplier<T> registrarSupplier) {
		System.out.println("> registerRegistrar "+clazz);
		// If multiple routes (through functional registration generated logic) lead to recording the same registrar,
		// they should all be the same supplier so it is ok to overwrite each other here. The key thing is that each
		// registrar supplier will only be called once.
		registrars.put(clazz, registrarSupplier);
	}
	
	public void invokeRegistrars() {
		for (Map.Entry<Class<? extends ImportBeanDefinitionRegistrar>, Supplier<? extends ImportBeanDefinitionRegistrar>> registrar: registrars.entrySet()) {
			// TODO should we call registerBeanDefinitions here or just do it in the generated code?
			registrar.getValue().get().registerBeanDefinitions(new StandardAnnotationMetadata(registrar.getKey()),context);
		}
	}

}
