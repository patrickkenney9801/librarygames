# Dependency image
FROM --platform=linux/amd64 golang:1.20.3 AS go-build

WORKDIR /tmp/librarygames/server

COPY server/go.mod server/go.sum /tmp/librarygames/server

RUN go mod download

COPY server/ /tmp/librarygames/server

ARG VERSION
ENV VERSION=${VERSION}
RUN go build -mod=readonly -ldflags "-X main.version=$VERSION" -race -o /tmp/librarygames-server ./main

# Sonar image
FROM --platform=linux/amd64 golang:1.20.3 AS sonar

RUN apt-get update && \
  apt-get install -y \
    unzip \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

ENV SONAR_SCANNER_VERSION=4.8.0.2856
RUN mkdir -p /tmp/sonar \
  && curl -sSLo /tmp/sonar/scanner.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SONAR_SCANNER_VERSION-linux.zip \
  && unzip -o /tmp/sonar/scanner.zip -d /usr/local/ \
  && rm -rf /tmp/sonar
RUN for exe in /usr/local/sonar-scanner-${SONAR_SCANNER_VERSION}-linux/bin/*; do ln -s ${exe} /usr/local/bin/$(basename "${exe}"); done

# Test image
FROM go-build AS go-test

COPY --from=sonar /usr/local/sonar-scanner-*-linux /usr/local/sonar-scanner-linux
RUN for exe in /usr/local/sonar-scanner-linux/bin/*; do ln -s ${exe} /usr/local/bin/$(basename "${exe}"); done

COPY sonar-project.properties /tmp/librarygames/sonar-project.properties

RUN go test -short -race -coverprofile=/tmp/cov.out `go list ./... | grep -v vendor/`
RUN go tool cover -func=/tmp/cov.out
RUN sed -i 's/github.com\/patrickkenney9801\/librarygames/\/tmp\/librarygames\/server/g' /tmp/cov.out

ARG VERSION
ARG SONAR_ADDRESS
ENV SONAR_ADDRESS=${SONAR_ADDRESS}
ENV VERSION=${VERSION}

WORKDIR /tmp/librarygames

RUN echo "sonar-scanner -Dsonar.host.url=$SONAR_ADDRESS -Dsonar.projectVersion=$VERSION -Dsonar.go.coverage.reportPaths=/tmp/cov.out" > sonar.sh

CMD ["bash", "sonar.sh"]

# Main image
FROM --platform=linux/amd64 gcr.io/distroless/base-debian11 AS go-release

USER nonroot:nonroot
WORKDIR /home/librarygames

COPY --from=go-build /tmp/librarygames-server /home/librarygames/librarygames-server

EXPOSE 8080
EXPOSE 8081

CMD ["/home/librarygames/librarygames-server"]
