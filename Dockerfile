# Build reproducible with the repository Gradle wrapper and Java 21.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --no-daemon bootJar -x test \
    && cp "$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -print -quit)" /tmp/diplomatic-fleet.jar

# Runtime: no build cache, no database secrets, non-root user.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd -r fleet && useradd -r -g fleet -d /app fleet
COPY --from=build /tmp/diplomatic-fleet.jar ./app.jar
USER fleet
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=50.0","-XX:InitialRAMPercentage=12.0","-jar","/app/app.jar"]
