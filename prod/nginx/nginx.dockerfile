FROM ghcr.io/cs-vsu-ru/site-web:latest as web

FROM mirror.gcr.io/nginx:1.25.5 as nginx

WORKDIR /etc/nginx

COPY config.nginx ./nginx.conf
COPY key.pem ./certs/key.pem
COPY crt.pem ./certs/crt.pem
COPY --from=web /web ./html
