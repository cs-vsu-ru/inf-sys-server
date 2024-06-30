# Серверная часть сайта Кафедры Информационных систем ФКН ВГУ

### [Ссылка на сайт](https://www.cs.vsu.ru/is)

### [Проект в Jira](https://krystall.atlassian.net/jira/software/projects/ISAPP/boards/2)

## Инструкция работы с репозиториями приложения

### Подключение по ssh

Для того, чтобы подключиться к серверу, нужно настроить VPN до сервера ВГУ.

[Инструкция подключения к VPN](https://noc.vsu.ru/support/vpn.html)

Креды - в сообщении от Ивана Кудинова под тегом `#VPN`

Далее в командной строке вводим команду и пароль из сообщения под тегом `#SSH`

После успешного подключения вы окажетесь в системе, где запущен Docker Compose проект.

В папке `~/prod` лежит копия папки `./prod` из этого репозитория (по крайней мере, мы стараемся держать ее актуальной).

В этой папке есть основной (и единственный) docker-compose.yaml, скрипт создания базы, сертификаты, Dockerfile для nginx, конфига nginx и файл `.env`, в котором указаны переменные окружения, которые автоматически считываются при запуске Docker Compose (там, в основном, пароли).

### Просмотр Docker контейнеров

```bash
docker ps --all
```
покажет список всех запущенных контейнеров. Вывод команды - ниже.

```
CONTAINER ID   IMAGE                                     COMMAND                  CREATED       STATUS                PORTS                                           NAMES
2c4f1846fc42   prod-nginx                                "/docker-entrypoint.…"   7 days ago    Up 2 days             80/tcp, 0.0.0.0:443->443/tcp, :::443->443/tcp   nginx
f9f38789b7e3   ghcr.io/cs-vsu-ru/inf-sys-parser:latest   "sh -c 'python manag…"   3 weeks ago   Up 2 days                                                             inf-sys-parser
1f19a530f6c9   ghcr.io/cs-vsu-ru/inf-sys-server:latest   "java --enable-previ…"   3 weeks ago   Up 2 days (healthy)                                                   inf-sys-server
303f63ae4732   mirror.gcr.io/postgres:16-alpine          "docker-entrypoint.s…"   3 weeks ago   Up 2 days (healthy)   5432/tcp                                        postgresql
23c6dac12a7b   mirror.gcr.io/redis:7.0.11                "docker-entrypoint.s…"   3 weeks ago   Up 2 days (healthy)   6379/tcp                                        redis
```

Приложение состоит из 5 контейнеров.

`prod-nginx` - собранный на основе запакованных gzip файлов из `ghcr.io/cs-vsu-ru/site-web` nginx. В нем прописаны пути до public урлов сервера и парсера.

`inf-sys-parser` - парсер расписания на Python из репозитория `github.com/cs-vsu-ru/inf-sys-parser`. Для запуска ему нужен Redis, Postgres и сервер.

`inf-sys-server` - сервер на Java, в котором реализованы CRUD операции над всеми сущностями сайта и LDAP авторизация пользователей (преподавателей). Для запуска ему нужен Postgres. Есть Healthcheck-проба.

`postgresql` - база данных. В ней есть две БД - inf-sys-server и inf-sys-parser. Каждая из них используется своим приложением. Скрипт создания пользователей и баз - `./prod/init-database.sh`.

`redis` - просто Redis. Как то работает, как то используется парсером. :)

Есть два важных Volume

`postgres-data` - все понятно, данные БД.

`files-volume` - динамический контент с сайта. Если загрузить на сервер через ручку `POST /upload-file` файл, то он будет храниться в этом Volume. Volume шарится на два контейнера - nginx-prod (настроен маппинг route на директорию) и inf-sys-server, через который файлы туда и загружаются. 

### Подключение к БД

```bash
docker exec -it 303f63ae4732 bash
```
, где 303f63ae4732 - CONTAINER_ID контейнера Postgres.

```bash
 psql --username "cs-pr-development" "inf-sys-server"
```
, если хотим подрубиться к базе парсера - то в команде меняем `inf-sys-server` на `inf-sys-parser`.

Теперь можно выполнять запросы, например, посмотреть все новости :) (не забываем ставить `;` после запроса)
```postgresql
SELECT * FROM news;
```

### Релиз

Во всех репозиториях (site-web, inf-sys-server, inf-sys-parser) настроена запускаемая вручную джоба паблиша контейнера в Registry. Она называется Publish Docker Image to Registry.

Обычный флоу релиза такой:

- Заводим МР в GitHub со своими изменениями
- Ждем аппрувов, вливаем МР в ветку `master` (либо `dev` в случае site-web)
- В репозитории идем на вкладку Actions, слева выбираем `Publish Docker Image to Registry`.
- Справа появится кнопка `Run workflow`. Нажимаем и выбираем ветку, на которой хотим запустить джобу.
- Ждем минутку-две, пока не появится зеленой галочки.
- Включаем VPN ВГУ, подключаемся по SSH к серверу.
- Дальше убиваем нужный контейнер 
    ```bash 
    docker container rm container_name
    ```
- Удаляем образ из файловой системы
    ```bash 
    docker images
    docker rmi image_id
    ```
- Запускаем компоуз по новой (будет запущен только один контейнер)
    ```bash 
    docker compose up -d
    ```

Note: на сервере запущен Self Hosted GitHub Runner (as a Service). Но он почему-то не берет в работу джобы из очереди. Можете починить)