--liquibase formatted sql

--changeset tukitoki:202405121600_data_employees context:local

-- employees data
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 1, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'https://www.vsu.ru/ru/persons/photo.php?p=24431', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 2, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', 'http://www.cs.vsu.ru:80/is/api/files/8d246826-b3df-4b4c-b338-5be4f214972dДесятирикова.jpg', 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 3, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/ccddd7f3-a456-4661-b737-e48faad4ccbeЕрмаков.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 4, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 5, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/c1d34efa-560d-4f27-b0f2-bc0fd5dc65feКоваль.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 6, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/6c3616e4-a0f2-42bf-84a8-325546bb9a87Малыхин.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 7, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/83be4eb3-4916-400e-8fe5-48e4417fcb4aМахортов.jpg', '2023-06-07 10:25', 1);