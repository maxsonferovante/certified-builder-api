# syntax=docker/dockerfile:1

FROM gradle:8.13.0-jdk23 as deps

WORKDIR /build

# Configure Maven mirrors and settings
RUN mkdir -p /root/.gradle && \
    echo "repositories { \
        maven { url 'https://maven.aliyun.com/repository/public' } \
        maven { url 'https://maven.aliyun.com/repository/google' } \
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' } \
        mavenCentral() \
        google() \
    }" > /root/.gradle/init.gradle

# Copy build files
COPY build.gradle settings.gradle ./

# Download dependencies with retry
RUN --mount=type=cache,target=/root/.gradle \
    gradle dependencies --no-daemon --info || \
    (sleep 5 && gradle dependencies --no-daemon --info) || \
    (sleep 10 && gradle dependencies --no-daemon --info)

################################################################################

FROM deps as package

WORKDIR /build

COPY ./src src/
RUN --mount=type=cache,target=/root/.gradle \
    gradle build -x test --no-daemon && \
    cp build/libs/*.jar app.jar

################################################################################

FROM eclipse-temurin:23-jre AS final

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

COPY --from=package /build/app.jar app.jar

EXPOSE 8081

ENTRYPOINT [ "java", "-jar", "app.jar" ]
