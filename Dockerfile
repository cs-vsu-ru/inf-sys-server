FROM openjdk:17

ARG GITHUB_REPO
LABEL org.opencontainers.image.source=https://github.com/${GITHUB_REPO}

COPY ./build/libs/inf-sys-server-0.0.1-SNAPSHOT.jar ./server.jar

ENTRYPOINT ["java", "--enable-preview", "-jar", "server.jar"]