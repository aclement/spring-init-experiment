package lib.multi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import slim.ModuleRoot;

@Configuration
@ModuleRoot
public class BarConfiguration {

	@Bean
	public Foo foo() {
		return new Foo();
	}

	@Bean
	public Bar bar(Foo foo) {
		return new Bar(foo);
	}

}
