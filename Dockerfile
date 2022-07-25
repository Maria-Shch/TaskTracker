FROM adoptopenjdk/openjdk15
ADD target/tracker-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]