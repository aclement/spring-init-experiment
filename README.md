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