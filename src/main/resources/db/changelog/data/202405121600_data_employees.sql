--liquibase formatted sql

--changeset tukitoki:202405121600_data_employees context:local

-- employees data
INSERT INTO employees(date_of_birth, has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience)
VALUES ('1999-01-01', true, '2024-05-12', 1, 'Кандидат наук', 'Доцент', '2000-09-01',
        'Николаевич', null, 'Заведующий кафедрой', '2005-09-01');
INSERT INTO employees(date_of_birth, has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience)
VALUES ('1999-01-01', true, '2024-05-12', 2, 'Доктор наук', 'Профессор', '1988-01-01',
        'Николаевна', 'http://www.cs.vsu.ru:80/is/api/files/b1fc4642-7201-439d-9a99-c9b6dba98383Десятирикова.pdf', 'Профессор', '1987-01-01');
INSERT INTO employees(date_of_birth, has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience)
VALUES ('1999-01-01', true, '2024-05-12', 3, '', '', '1995-01-01',
        'Викторович', 'http://www.cs.vsu.ru:80/is/api/files/d9be07b7-6454-4835-bb27-589c07511109Ермаков.pdf', 'Старший преподаватель', '2017-09-01');
INSERT INTO employees(date_of_birth, has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience)
VALUES ('1999-01-01', true, '2024-05-12', 4, 'Кандидат наук', 'Без ученого звания', '1984-01-01',
        'Алексеевич', 'http://www.cs.vsu.ru:80/is/api/files/52d649cf-65dc-4834-962c-72fbec74f396Зуев.pdf', 'Доцент', '2012-09-01');
INSERT INTO employees(date_of_birth, has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience)
VALUES ('1999-01-01', true, '2024-05-12', 5, '', '', '1989-01-01',
        'Сергеевич', 'http://www.cs.vsu.ru:80/is/api/files/421e75c1-3be2-4392-a80d-b528534d900bКоваль.pdf', 'Старший преподаватель', '1996-09-01');
INSERT INTO employees(date_of_birth, has_lessons,
                      created_at, user_id, academic_degree, academic_title, experience,
                      patronymic, plan, post, professional_experience)
VALUES ('1999-01-01', true, '2024-05-12', 6, 'Кандидат наук', 'Без ученого звания', '2013-01-01',
        'Юрьевич', 'http://www.cs.vsu.ru:80/is/api/files/011e67da-0129-48db-82f2-dfe744c85f9cМалыхин.pdf', 'Старший преподаватель', '2014-09-01');
