--liquibase formatted sql

--changeset nagorniy:202604091201_data_tab_content context:local

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_about, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/about' AND sp.id = 1;

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_education, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/education' AND sp.id = 1;

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_exams, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/exams' AND sp.id = 1;

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_students, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/students' AND sp.id = 1;

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_important, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/important' AND sp.id = 1;

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_miscellaneous, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/miscellaneous' AND sp.id = 1;

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_contacts, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/contacts' AND sp.id = 1;

INSERT INTO tab_content (tab_id, content)
SELECT nt.id, COALESCE(sp.content_confidential, '')
FROM navigation_tabs nt, static_pages sp
WHERE nt.url = '/confident' AND sp.id = 1;

-- Для вкладок без соответствия в static_pages (Расписание, Сотрудники)
-- создаём пустые записи, если их ещё нет
INSERT INTO tab_content (tab_id, content)
SELECT nt.id, ''
FROM navigation_tabs nt
WHERE NOT EXISTS (SELECT 1 FROM tab_content tc WHERE tc.tab_id = nt.id);