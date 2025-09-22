FROM gradle:9.1.0-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test
RUN ls -la /home/gradle/src/build/libs

FROM eclipse-temurin:21.0.7_6-jre-alpine-3.21
EXPOSE 8010:8080
COPY --link --from=build /home/gradle/src/build/libs/galahad.jar /home/gradle/src/application.properties ./
ENTRYPOINT ["java", "-Xms4g", "-Xmx8g", "-Dspring.profiles.active=prod", "-jar","galahad.jar"]
