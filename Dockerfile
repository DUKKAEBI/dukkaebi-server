FROM eclipse-temurin:21
COPY ./build/libs/dukkaebi-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]