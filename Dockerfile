FROM maven:3.8.5-amazoncorretto-8 AS build
WORKDIR /build

RUN yum install tar gzip -y

COPY java/pom.xml /build/pom.xml
COPY scripts scripts

RUN mvn --version

COPY java .

ARG GIT_BRANCH
ARG GIT_TAG

# Build the application.
#
RUN ./scripts/build.sh

# Collect assembled jar for publishing
ARG BUILD_ARTIFACTS_RELEASE=/build/target/*.jar
ARG BUILD_ARTIFACTS_JAVA=/build/target/*.jar

# Generate Veracode Artifact
RUN tar -cvzf /java.tar.gz /build/target/*.jar
ARG BUILD_ARTIFACTS_VERACODE=/java.tar.gz
ARG BUILD_ARTIFACTS_POM=/build/pom.xml

# We only care about publishing a jar
FROM scratch
