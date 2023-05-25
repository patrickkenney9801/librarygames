# Dependency image
FROM --platform=linux/amd64 openjdk:11-jdk-slim-buster AS mvn-build

RUN apt-get update && \
  apt-get install -y --no-install-recommends \
    maven \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /tmp/librarygames

COPY pom.xml /tmp/librarygames/pom.xml
RUN mvn verify clean --fail-never

COPY src/ /tmp/librarygames/src
COPY pbs/ /tmp/librarygames/src/main/proto
COPY .classpath /tmp/librarygames/.classpath
COPY .project /tmp/librarygames/.project

RUN mvn compile assembly:single install

# Main image
FROM --platform=linux/amd64 ubuntu:20.04 AS mvn-release

RUN apt-get update && \
  apt-get install -y --no-install-recommends \
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
