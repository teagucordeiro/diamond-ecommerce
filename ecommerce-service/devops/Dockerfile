FROM maven:3.9.4-eclipse-temurin-21 AS build
RUN mkdir /srv/app
COPY . /srv/app
WORKDIR /srv/app
RUN mvn clean install

FROM amazoncorretto:21-alpine-jdk
RUN mkdir /srv/app
ENV JAR_FILE=target/*.jar
COPY --from=build /srv/app/${JAR_FILE} /srv/app/app.jar
WORKDIR /srv/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

