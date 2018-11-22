package app.guard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BarConfiguration {

	@Bean
	public Bar bar(Foo foo) {
		return new Bar(foo);
	}

}
