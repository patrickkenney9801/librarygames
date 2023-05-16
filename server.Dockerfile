# Dependency image
FROM --platform=linux/amd64 golang:1.20.3 AS go-build

ARG VERSION

WORKDIR /tmp/librarygames/server

COPY server/go.mod server/go.sum /tmp/librarygames/server

RUN go mod download

COPY server/ /tmp/librarygames/server

ENV VERSION=${VERSION}
RUN go build -mod=readonly -ldflags "-X main.version=$VERSION" -race -o /tmp/librarygames-server ./main

# Test image
FROM go-build AS go-test

CMD ["go", "test", "-v", "./..."]

# Main image
FROM --platform=linux/amd64 gcr.io/distroless/base-debian11 AS go-release

USER nonroot:nonroot
WORKDIR /home/librarygames

COPY --from=go-build /tmp/librarygames-server /home/librarygames/librarygames-server

EXPOSE 8080
EXPOSE 8081

CMD ["/home/librarygames/librarygames-server"]
