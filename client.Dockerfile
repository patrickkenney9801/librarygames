# Dependency image
FROM --platform=linux/amd64 openjdk:11-jdk-slim-buster AS mvn-build

RUN apt-get update && \
  apt-get install -y \
    maven \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY src/ /tmp/librarygames/src
COPY .classpath /tmp/librarygames/.classpath
COPY .project /tmp/librarygames/.project
COPY pom.xml /tmp/librarygames/pom.xml

WORKDIR /tmp/librarygames
RUN mvn clean compile assembly:single install

# Main image
FROM --platform=linux/amd64 ubuntu:20.04

RUN apt-get update && \
  apt-get install -y \
    openjdk-11-jre \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN useradd -ms /bin/bash librarygames
USER librarygames
WORKDIR /home/librarygames
ENV DISPLAY=:0

ARG VERSION

COPY --from=mvn-build /tmp/librarygames/target/librarygames-${VERSION}.jar /home/librarygames/librarygames.jar
COPY --from=mvn-build /tmp/librarygames/target/lib/ /home/librarygames/lib

CMD ["java", "-jar", "/home/librarygames/librarygames.jar"]
