--liquibase formatted sql

--changeset tukitoki:202405121600_data_employees context:local

-- employees data
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 1, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 2, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', 'http://www.cs.vsu.ru:80/is/api/files/52d649cf-65dc-4834-962c-72fbec74f396Зуев.pdf', 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 3, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 4, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 5, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 6, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);
INSERT INTO employees(has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience, image_url, last_modified_at, last_modified_by_id)
VALUES (true, '2023-06-07 10:25', 7, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01', 'http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg', '2023-06-07 10:25', 1);