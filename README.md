Demo of an APT plugin that converts `@Configuration` to
functional bean registrations, which are known to be faster and also
more amenable to AOT compilation (native image building).

Takes this:

```java
@Configuration
public class SampleConfiguration {

    @Bean
    public Foo foo() {
        return new Foo();
    }

    @Bean
    public Bar bar(Foo foo) {
        return new Bar(foo);
    }
    
}
```

and transforms it to this:

```java
@Configuration
public class SampleConfiguration implements ApplicationContextInitializer<GenericApplicationContext> {

    @Bean
    public Foo foo() {
        return new Foo();
    }

    @Bean
    public Bar bar(Foo foo) {
        return new Bar(foo);
    }

    @Override
    public void initialize(GenericApplicationContext context) {
        context.registerBean(MyConfig.class);
        context.registerBean(Foo.class, () -> context.getBean(MyConfig.class).foo());
        context.registerBean(Bar.class, () -> context.getBean(MyConfig.class).bar(context.getBean(Foo.class)));
    }
    
}
```

You then need a Spring application bootstrap utility that recognizes
the `ApplicationContextInitializer` and treats it in a special way.

Build and run:

```
$ ./mvnw package
$ java -jar samples/application/target/*.jar
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v2.1.0.RELEASE)

...
Bar: com.acme.Bar@7c3ebc6b
```

The command line runner output comes out on stdout after the Spring Boot app has started.

## Building

You can build and run everything from the command line, or from an IDE. If you are using https://github.com/jbosstools/m2e-apt/issues/64[Eclipse], you need to disable the Maven project nature for the "processor" project (or work with it in a different workspace). It only has 2 dependencies, so it's quite easy to manually set up the classpath for that project (then you have to build it on the command line before you refresh the sample apps).

You can debug the APT processor by running on the command line and attaching a debugger, e.g:

```
(cd modules/tests-lib/; MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000" mvn clean compile)
```
