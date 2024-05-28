# Инструкция по локальному запуску приложения inf-sys-server

## Список необходимого ПО
- #### Docker
- #### [EnvFile plugin](https://plugins.jetbrains.com/plugin/7861-envfile) для Intellij IDEA

## Порядок действий для локального запуска

### Создание .env файла
- Создать в директории `localrun` файл `.env`
- Прописать в созданном файле требуемые переменные окружения в формате `ENV_NAME=ENV_VAL`, каждую переменную писать с новой строки
    <details>
        <summary><strong>Список обязательных переменных окружения</strong></summary>
        <ul>
            <li><pre>POSTGRES_PASSWORD</pre></li>
            <li><pre>SERVER_PORT</pre></li>
            <li><pre>BASE_FILES_FOLDER</pre></li>
            <li><pre>PARSER_URL</pre></li>
        </ul>
    </details>
    <details>
        <summary><strong>Пример содержимого .env файла</strong></summary>
        <pre>POSTGRES_PASSWORD=myPasswordForDB</pre>
    </details>
- Файл с переменными окружения автоматически будет прочитан Docker'ом и IDEA при запуске контейнера/приложения

### Запуск
- Запустить docker-compose из IDE или командой `docker compose up`
- Убедиться, что [EnvFile plugin](https://plugins.jetbrains.com/plugin/7861-envfile) установлен
- В Run configurations в IDEA выбрать и запустить конфигурацию `app-localrun`

## Разработчику
- Поддерживай конфигурацию в актуальном состоянии
- Следи за тем, чтобы после твоих изменений локальный запуск работал
- При необходимости дополни список необходимых переменных окружения