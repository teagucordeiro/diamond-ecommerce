FROM maven:3.9.4-eclipse-temurin-21 AS base
RUN mkdir /srv/app
COPY . /srv/app
WORKDIR /srv/app
RUN mvn clean install

FROM amazoncorretto:21-alpine-jdk as app
RUN mkdir /srv/app
ENV JAR_FILE=target/*.jar
COPY --from=base /srv/app/${JAR_FILE} /srv/app/app.jar
WORKDIR /srv/app
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM base as dev
COPY . /srv/app
WORKDIR /srv/app
ENTRYPOINT ["/bin/sh","-c", "mvn spring-boot:run"]