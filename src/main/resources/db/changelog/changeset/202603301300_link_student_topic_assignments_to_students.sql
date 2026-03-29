--liquibase formatted sql

--changeset codex:202603301300

ALTER TABLE student_topic_assignments
    ADD COLUMN student_id BIGINT;

UPDATE student_topic_assignments sta
SET student_id = s.id
FROM students s
         JOIN users u ON u.id = s.user_id
WHERE LOWER(TRIM(sta.student_login)) = LOWER(TRIM(u.login));

ALTER TABLE student_topic_assignments
    ADD CONSTRAINT fk_student_topic_assignments_student
        FOREIGN KEY (student_id) REFERENCES students (id);

CREATE UNIQUE INDEX IF NOT EXISTS uniq_student_topic_assignments_student_id
    ON student_topic_assignments (student_id)
    WHERE student_id IS NOT NULL;
