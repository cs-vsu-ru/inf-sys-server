--liquibase formatted sql

--changeset tukitoki:202405091316_data_sliders context:local

-- sliders data

INSERT INTO sliders(id, imageurl, url_to, title, create_at)

VALUES (3,'http://www.cs.vsu.ru:80/is/api/files/7df50971-9771-4646-b00e-55443beb5942Лаборатория.png',NULL,'Лаборатория телекоммуникаций и интернета вещей', CURRENT_TIMESTAMP),
       (9,'http://www.cs.vsu.ru:80/is/api/files/783ff4e6-74ab-4c4a-911b-454f61cf5d06Л Электротехники2.jpg',NULL,'Лаборатория электротехники, электроники и схемотехники', CURRENT_TIMESTAMP),
       (10,'http://www.cs.vsu.ru:80/is/api/files/edeed659-a72a-4b5c-9438-075127f7642fCertificatOfAutorization_Huawei.jpg','https://www.cs.vsu.ru/2022/02/na-fkn-otkryli-akademiyu-huawei-huawei-ict-academy/','ИКТ Академия Huawei', CURRENT_TIMESTAMP);

SELECT setval('slider_sequence', 10, true);