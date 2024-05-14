--liquibase formatted sql

--changeset tukitoki:202405121550_data_users context:local

-- users data
INSERT INTO users (email, first_name, last_name, password, role)
VALUES ('borisov@sc.vsu.ru', 'Дмитрий', 'Борисов', '$2a$10$yjMlwVQwqKpzACCOdZs22uIgmJ.RxKT.VhwxcteAAfZkHRxpZKUNK', null);
INSERT INTO users (email, first_name, last_name, password, role)
VALUES ('science2000@yandex.ru', 'Елена', 'Десятирикова', '$2a$10$55KDhpxzQKEZmzhce0Oj2ee8.vGXRD8E/8zYkj433MNgjWeglLm6.', null);
INSERT INTO users (email, first_name, last_name, password, role)
VALUES ('micermakov@yandex.ru', 'Михаил', 'Ермаков', '$2a$10$MW2y7UsS5DJpiONXLTXxFuTD9aZV7nmUDl5F1jNqzw9ikPyeOL3ta', null);
INSERT INTO users (email, first_name, last_name, password, role)
VALUES ('zsa_zuev@mail.ru', 'Сергей', 'Зуев', '$2a$10$Xcp7cSctpANe.F/VXDRfKea4cvvLMFz5rZMNsrY7Aw/ypRX7WkZPe', null);
INSERT INTO users (email, first_name, last_name, password, role)
VALUES ('koval@sc.vsu.ru', 'Андрей', 'Коваль', '$2a$10$heH0uAooDk8hv4ly6YgpM.9KF0qYR6unFLdYzkSfGTCMzeikfhgpO', null);
INSERT INTO users (email, first_name, last_name, password, role)
VALUES ('mal_and@inbox.ru', 'Андрей', 'Малыхин', '$2a$10$g1kLZnVxJ7x7VtrRLmBYpuUm5sHSIxy6Kttwv7D4e69aK1ck.QCkm', null);
