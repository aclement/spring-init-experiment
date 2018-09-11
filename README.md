Demo of a Byte Buddy plugin that converts `@Configuration` to
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
$ java -jar target/*.jar
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v2.1.0.BUILD-SNAPSHOT)

...
Bar: com.acme.Bar@7c3ebc6b
```

The command line runner output comes out on stdout after the Spring Boot app has started.

Build a native image:

```
$ CP=`java -jar target/*.jar --thin.classpath --thin.profile=graal`
$ sdkman use java 1.0.0-rc5-graal
$ native-image -H:Name=target/simple -H:ReflectionConfigurationFiles=simple.json --report-unsupported-elements-at-runtime -cp $CP com.acme.SimpleApplication
$ ./target/simple
...
Bar: com.acme.Bar@7f1d27422ad8
```

N.B. the main class in the native image is `SimpleApplication` which is not a Spring Boot app. Spring Boot relies on `spring.factories` for a lot of its behaviour at runtime, and a Graal native image cannot use that mechanism currently.

Native image generation also works for the `SampleApplication`, but requires Graal rc5 (rc6 is broken):

```
$ mvn clean install
$ CP=`java -jar target/spring-init-experiment-1.0-SNAPSHOT.jar --thin.classpath --thin.profile=graal`
$ native-image -Daj.weaving.verbose=true -J-javaagent:$HOME/.m2/repository/org/aspectj/aspectjweaver/1.9.1/aspectjweaver-1.9.1.jar -H:Name=target/app -H:IncludeResources='META-INF/spring.factories|org/springframework/boot/logging/.*' -H:ReflectionConfigurationFiles=app.json --report-unsupported-elements-at-runtime -cp $CP com.acme.SampleApplication
$ ./target/app -Dorg.springframework.boot.logging.LoggingSystem=org.springframework.boot.logging.java.JavaLoggingSystem
11:09:17.864 [main] DEBUG org.springframework.boot.context.logging.ClasspathLoggingApplicationListener - Application started with classpath: unknown

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                        

11:09:17.865 [main] INFO com.acme.SampleConfiguration - Initializing
...
11:09:17.866 [main] INFO com.acme.SampleConfiguration - Creating: com.acme.Bar@7f1a6ad164a8
11:09:17.866 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory'
11:09:17.866 [main] INFO org.springframework.boot.SpringApplication - Started application in 0.008 seconds (JVM running for 0.009)
11:09:17.866 [main] INFO com.acme.SampleConfiguration - Bar: com.acme.Bar@7f1a6ad164a8

```

For some reason this doesn't work without the `-Daj.weaving.verbose=true`.
