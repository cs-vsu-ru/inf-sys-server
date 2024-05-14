--liquibase formatted sql

--changeset tukitoki:202405091316_data_sliders context:local

-- sliders data
INSERT INTO sliders(create_at, imageurl, title, updated_at, url_to)
VALUES (CURRENT_TIMESTAMP,
        'http://www.cs.vsu.ru:80/is/api/files/7df50971-9771-4646-b00e-55443beb5942Лаборатория.png',
        'Лаборатория телекоммуникаций и интернета вещей', null, null);
INSERT INTO sliders(create_at, imageurl, title, updated_at, url_to)
VALUES (CURRENT_TIMESTAMP,
        'http://www.cs.vsu.ru:80/is/api/files/783ff4e6-74ab-4c4a-911b-454f61cf5d06Л Электротехники2.jpg',
        'Лаборатория электротехники, электроники и схемотехники', null, null);
INSERT INTO sliders(create_at, imageurl, title, updated_at, url_to)
VALUES (CURRENT_TIMESTAMP,
        'http://www.cs.vsu.ru:80/is/api/files/edeed659-a72a-4b5c-9438-075127f7642fCertificatOfAutorization_Huawei.jpg',
        'ИКТ Академия Huawei', null,
        'https://www.cs.vsu.ru/2022/02/na-fkn-otkryli-akademiyu-huawei-huawei-ict-academy/');