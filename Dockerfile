FROM gradle:jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM openjdk:15.0.1

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/spring-boot-application.jar"]

#FROM openjdk:15.0.1
#ADD build/libs/torrentsearch-0.0.2.jar torrent-search.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "torrent-search.jar"]
