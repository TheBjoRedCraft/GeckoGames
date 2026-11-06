FROM gradle:8-jdk21 AS build

WORKDIR /build

COPY . .
RUN gradle :surf-gecko-server:shadowJar --no-daemon -q
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /server

COPY --from=build /build/surf-gecko-server/build/libs/surf-gecko-server.jar server.jar
EXPOSE 25565
ENTRYPOINT ["sh", "-c", "exec java $JVM_OPTS -XX:+UseZGC -XX:+ZGenerational -XX:+AlwaysPreTouch -XX:+DisableExplicitGC -jar server.jar"]
