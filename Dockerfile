# syntax=docker/dockerfile:1.0-experimental
FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app
ARG HOME=/root
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY modules ./modules
COPY samples ./samples
RUN --mount=type=cache,target=${HOME}/.m2 ./mvnw dependency:get -Dartifact=org.springframework.boot.experimental:spring-boot-thin-launcher:1.0.17.RELEASE:jar:exec -Dtransitive=false
RUN --mount=type=cache,target=${HOME}/.m2 ./mvnw install -DskipTests
VOLUME ${HOME}/.m2

FROM dsyer/graalvm-native-image:1.0.0-rc7 as native
ARG HOME=/root
ARG SAMPLE=server
ARG THINJAR=${HOME}/.m2/repository/org/springframework/boot/experimental/spring-boot-thin-launcher/1.0.17.RELEASE/spring-boot-thin-launcher-1.0.17.RELEASE-exec.jar
WORKDIR /workspace/app
COPY --from=build ${HOME}/.m2 ${HOME}/.m2
COPY --from=build /workspace/app/samples/${SAMPLE}/* /workspace/app/
ENV PATH="${PATH}:/usr/lib/graalvm/bin"
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
RUN native-image --no-server --static -J-XX:+UnlockExperimentalVMOptions -J-XX:+UseCGroupMemoryLimitForHeap -J-javaagent:${HOME}/.m2/repository/org/aspectj/aspectjweaver/1.9.2/aspectjweaver-1.9.2.jar -Dio.netty.noUnsafe=true -Dio.netty.noJdkZlibDecoder=true -Dio.netty.noJdkZlibEncoder=true -H:Name=target/app -H:ReflectionConfigurationFiles=`echo *.json | tr ' ' ,` -H:ReflectionConfigurationResources=META-INF/library.json -H:IncludeResources='META-INF/.*.json|META-INF/spring.factories|org/springframework/boot/logging/.*' --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,org.springframework.core.io.VfsUtils,io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator,io.netty.handler.ssl.ReferenceCountedOpenSslEngine  --report-unsupported-elements-at-runtime -cp `java -jar ${THINJAR} --thin.archive=target/dependency --thin.classpath --thin.profile=graal` app.main.SampleApplication
#COPY target/app /workspace/app/

#FROM alpine
#WORKDIR /workspace/app
#VOLUME /tmp
#COPY --from=native /workspace/app/app .
#EXPOSE 8080
#ENTRYPOINT ["./app"]