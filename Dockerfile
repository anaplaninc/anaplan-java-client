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

# AU 7/12/22 - At present, Workiva/plugin-anaplan-automation requires a copy of the
# anaplan-connect-*-jar-with-dependencies.jar be placed in its lib/ folder in order to build correctly.
# When Workiva/plugin-anaplan-automation gets hookedup to Workiva CI I intend to alter its build process
# to pull the regular thin anaplan-connect jar from maven, but for now we will continue to produce the
# anaplan-connect-*-jar-with-dependencies.jar for backwards compatilibity
RUN mkdir /build/artifacts/ && \
    mv /build/target/anaplan-connect-*-jar-with-dependencies.jar /build/artifacts/
ARG BUILD_ARTIFACTS_RELEASE=/build/artifacts/*.jar

# Collect assembled jar for publishing
ARG BUILD_ARTIFACTS_JAVA=/build/target/*.jar

# Generate Veracode Artifact
RUN tar -cvzf /java.tar.gz /build/target/*.jar
ARG BUILD_ARTIFACTS_VERACODE=/java.tar.gz
ARG BUILD_ARTIFACTS_POM=/build/pom.xml

# We only care about publishing a jar
FROM scratch
