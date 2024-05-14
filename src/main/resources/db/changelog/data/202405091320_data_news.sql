--liquibase formatted sql

--changeset tukitoki:202405091320_data_news context:local

-- news data
INSERT INTO news(content, image_link, publication_at, title)
VALUES ('<p>На кафедре информационных систем открылась учебно-научная лаборатория электротехники, электроники и схемотехники.&nbsp;</p>',
        'http://www.cs.vsu.ru:80/is/api/files/662e4b81-b73b-4013-b767-c9c4b7f939edЛ Электротехники2.jpg',
        '2023-06-07',
        'Учебно-научная лаборатория электротехники, электроники и схемотехники');
INSERT INTO news(content, image_link, publication_at, title)
VALUES ('<p>На кафедре информационных систем открылась учебно-научная &nbsp;лаборатория телекоммуникаций и интернета вещей</p>',
        'http://www.cs.vsu.ru:80/is/api/files/70c85ca7-103c-4b73-9039-b9798a699d76Л телекоммуникаций.jpg',
        '2023-06-07',
        'Учебно-научная лаборатория телекоммуникаций и интернета вещей');