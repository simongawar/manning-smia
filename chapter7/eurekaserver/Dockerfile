
# STAGE 1: BUILD (Compiles and Packages the JAR)

# Using a JDK image for the build environment (Amazon Corretto 21 Alpine).


FROM amazoncorretto:21-alpine AS build

# Add Maintainer Info

LABEL maintainer="Simon Gawr   g.dakthak@gmail.com"

# The application's jar file is expected to be passed as an argument

ARG JAR_FILE

# Copy the fat JAR file into the container

COPY ${JAR_FILE} app.jar

# STAGE 2: LAYER EXTRACTION (Extracts the layers for caching)

# This stage uses the layertools feature built into Spring Boot JARs.

FROM amazoncorretto:21-alpine AS extractor

# Copy the JAR from the build stage

COPY --from=build /app.jar app.jar

# Use the layertools command to extract the contents into separate directories (layers)

RUN java -Djarmode=layertools -jar app.jar extract --destination extracted


# STAGE 3: RUNTIME (The final, minimal image for execution)

# CORRECTED: Switched back to the official and guaranteed-to-exist 'amazoncorretto:21-alpine'

# tag for the final image, as the JRE-only Alpine tag is not published for Corretto 21.


FROM amazoncorretto:21-alpine

# Add volume pointing to /tmp (Standard practice for Spring Boot temp files)

VOLUME /tmp

# Create a non-root user and switch to it for better security

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring

# Copy the layers in the recommended order (least frequently changing first)

COPY --from=extractor extracted/dependencies/ ./
COPY --from=extractor extracted/spring-boot-loader/ ./
COPY --from=extractor extracted/snapshot-dependencies/ ./
COPY --from=extractor extracted/application/ ./

# Command to execute the application using the Spring Boot Loader

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]