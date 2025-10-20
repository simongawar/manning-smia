# STAGE 1: BUILD (Compiles and Packages the JAR)
FROM amazoncorretto:21-alpine AS build

LABEL maintainer="Simon Gawr <g.dakthak@gmail.com>"

# The application's JAR file is expected to be passed as an argument
ARG JAR_FILE=target/licensing-service.jar

# Copy the fat JAR file into the container
COPY ${JAR_FILE} app.jar

# STAGE 2: LAYER EXTRACTION (Extracts the layers for caching)
FROM amazoncorretto:21-alpine AS extractor

# Copy the JAR from the build stage
COPY --from=build /app.jar app.jar

# Use Spring Boot layertools to extract layers
RUN java -Djarmode=layertools -jar app.jar extract --destination extracted
# STAGE 3: RUNTIME (Minimal image for execution)
FROM amazoncorretto:21-alpine

# Add volume for Spring Boot temp files
VOLUME /tmp

# Create a non-root user for better security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring

# Copy layers in optimal order
COPY --from=extractor extracted/dependencies/ ./
COPY --from=extractor extracted/spring-boot-loader/ ./
COPY --from=extractor extracted/snapshot-dependencies/ ./
COPY --from=extractor extracted/application/ ./

# Run the application using Spring Boot's JarLauncher
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]