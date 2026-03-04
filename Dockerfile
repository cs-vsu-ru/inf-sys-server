FROM eclipse-temurin:17-jre

ARG GITHUB_REPO
LABEL org.opencontainers.image.source=https://github.com/${GITHUB_REPO}

COPY ./build/libs/inf-sys-server-0.0.1-SNAPSHOT.jar ./server.jar

ENTRYPOINT ["java", "--enable-preview", "-jar", "server.jar"]
