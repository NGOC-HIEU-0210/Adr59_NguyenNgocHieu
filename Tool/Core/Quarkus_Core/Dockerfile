# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
# cache ~/.m2 giữa các lần build (cần BuildKit - mặc định trong Docker mới)
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /deployments
RUN useradd -r -u 1001 quarkus
COPY --from=build --chown=1001 /build/target/quarkus-app/lib/ ./lib/
COPY --from=build --chown=1001 /build/target/quarkus-app/*.jar ./
COPY --from=build --chown=1001 /build/target/quarkus-app/app/ ./app/
COPY --from=build --chown=1001 /build/target/quarkus-app/quarkus/ ./quarkus/
USER 1001
EXPOSE 8080
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar quarkus-run.jar"]
