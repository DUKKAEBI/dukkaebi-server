FROM eclipse-temurin:21

# Python과 C++ 컴파일러 설치
RUN apt-get update && \
    apt-get install -y \
    python3 python3-pip \
    g++ build-essential \
    && apt-get clean

COPY ./build/libs/dukkaebi-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]