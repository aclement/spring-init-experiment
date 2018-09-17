package app.main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

@Configuration
public class SampleConfiguration {

    @Value("${app.value}")
    private String message;

    @Bean
    public Foo foo() {
        return new Foo();
    }

    @Bean
    public Bar bar(Foo foo) {
        return new Bar(foo);
    }

    @Bean
    public CommandLineRunner runner(Bar bar) {
        return args -> {
            System.out.println("Message: " + message);
            System.out.println("Bar: " + bar);
            System.out.println("Foo: " + bar.getFoo());
        };
    }

//    public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
//        return new Initializer();
//    }
//
//    private static class Initializer
//            implements ApplicationContextInitializer<GenericApplicationContext> {
//
//        @Override
//        public void initialize(GenericApplicationContext context) {
//            context.registerBean(SampleConfiguration.class);
//            context.registerBean("foo", Foo.class,
//                    () -> context.getBean(SampleConfiguration.class).foo());
//            context.registerBean("bar", Bar.class, () -> context
//                    .getBean(SampleConfiguration.class).bar(context.getBean(Foo.class)));
//            context.registerBean("runner", CommandLineRunner.class,
//                    () -> context.getBean(SampleConfiguration.class)
//                            .runner(context.getBean(Bar.class)));
//        }
//
//    }
}
