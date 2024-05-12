--liquibase formatted sql

--changeset tukitoki:202405091316_events_data context:local

-- events data
INSERT INTO events(content, end_date_time, last_modified_date_time,
                   publication_date_time, start_date_time, title)
VALUES ('<p>заседание кафедры информационных систем</p>', null, '2023-06-07 10:25',
        '2023-05-31 10:47', '2023-06-01 10:00', 'Заседание кафедры');
INSERT INTO events(content, end_date_time, last_modified_date_time,
                   publication_date_time, start_date_time, title)
VALUES ('1 июня будет проводиться защита курсовых работ студентами 3 курса всех профилей', null, '2023-06-07 10:25',
        '2023-05-31 14:15', '2023-06-01 11:00', 'Защита курсовых работ');
INSERT INTO events(content, end_date_time, last_modified_date_time,
                   publication_date_time, start_date_time, title)
VALUES ('', null, null,
        '2023-06-07 10:24', '2023-06-14 14:00', 'Заседание кафедры');
INSERT INTO events(content, end_date_time, last_modified_date_time,
                   publication_date_time, start_date_time, title)
VALUES ('Заседание кафедры в ауд. 387', null, null,
        '2023-06-30 15:40', '2023-07-03 13:00', 'Заседание кафедры');
INSERT INTO events(content, end_date_time, last_modified_date_time,
                   publication_date_time, start_date_time, title)
VALUES ('Заседание кафедры', null, null,
        '2023-09-16 10:22', '2023-08-31 15:00', 'Заседание кафедры');
INSERT INTO events(content, end_date_time, last_modified_date_time,
                   publication_date_time, start_date_time, title)
VALUES ('Заседание кафедры информационных систем', null, null,
        '2024-04-27 10:53', '2024-05-02 13:00', 'Заседание кафедры информационных систем');
INSERT INTO events(content, end_date_time, last_modified_date_time,
                   publication_date_time, start_date_time, title)
VALUES ('Прием задолженностей по дисциплинам кафедры информационных систем', null, null,
        '2024-04-27 10:52', '2024-05-02 15:00', 'Прием задолженностей');