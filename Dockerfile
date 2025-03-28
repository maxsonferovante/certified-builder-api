FROM eclipse-temurin:23-jre

WORKDIR /app

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

COPY ./build/libs/certified-builder-api-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/application.properties application.properties

EXPOSE 8081

ENTRYPOINT [ "java", "-jar", "app.jar" ]
