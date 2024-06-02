FROM ghcr.io/cs-vsu-ru/site-web:latest as web

FROM mirror.gcr.io/nginx:1.25.5 as nginx

WORKDIR /etc/nginx

COPY ./nginx/config.nginx ./templates/nginx.conf.conf
COPY ./nginx/key.pem ./certs/key.pem
COPY ./nginx/crt.pem ./certs/crt.pem
COPY --from=web /web ./html