# Stage 1: Build
FROM amazoncorretto:21-alpine AS build

LABEL maintainer="Illary Huaylupo <illaryhs@gmail.com>"

ARG JAR_FILE=target/organization-service.jar
COPY ${JAR_FILE} app.jar

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf /app.jar)

# Stage 2: Runtime
FROM amazoncorretto:21-alpine

VOLUME /tmp
WORKDIR /app

ARG DEPENDENCY=target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.optimagrowth.organization.OrganizationServiceApplication"]