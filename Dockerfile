FROM openjdk:15.0.1
ADD build/libs/torrentsearch* torrent-search.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "torrent-search.jar"]