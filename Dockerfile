FROM openjdk:15.0.1 AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
RUN chmod +x gradlew
RUN ./gradlew build || return 0 
COPY . .
RUN ./gradlew build

FROM openjdk:15.0.1
ENV ARTIFACT_NAME=torrentsearch-0.0.2.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "torrent-search.jar"]

#FROM openjdk:15.0.1
#ADD build/libs/torrentsearch-0.0.2.jar torrent-search.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "torrent-search.jar"]
