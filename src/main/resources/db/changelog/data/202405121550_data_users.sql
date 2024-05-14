--liquibase formatted sql

--changeset tukitoki:202405121550_data_users context:local

-- users data
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('borisov@sc.vsu.ru', 'Дмитрий', 'Борисов', 'borisov','$2a$10$yjMlwVQwqKpzACCOdZs22uIgmJ.RxKT.VhwxcteAAfZkHRxpZKUNK', null);
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('science2000@yandex.ru', 'Елена', 'Десятирикова', 'science2000', '$2a$10$55KDhpxzQKEZmzhce0Oj2ee8.vGXRD8E/8zYkj433MNgjWeglLm6.', null);
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('micermakov@yandex.ru', 'Михаил', 'Ермаков', 'micermakov','$2a$10$MW2y7UsS5DJpiONXLTXxFuTD9aZV7nmUDl5F1jNqzw9ikPyeOL3ta', null);
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('zsa_zuev@mail.ru', 'Сергей', 'Зуев', 'zuev', '$2a$10$Xcp7cSctpANe.F/VXDRfKea4cvvLMFz5rZMNsrY7Aw/ypRX7WkZPe', null);
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('koval@sc.vsu.ru', 'Андрей', 'Коваль', 'koval', '$2a$10$heH0uAooDk8hv4ly6YgpM.9KF0qYR6unFLdYzkSfGTCMzeikfhgpO', null);
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('mal_and@inbox.ru', 'Андрей', 'Малыхин', 'malihin', '$2a$10$g1kLZnVxJ7x7VtrRLmBYpuUm5sHSIxy6Kttwv7D4e69aK1ck.QCkm', null);
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('', 'Кудинов', 'Иван', 'kudinov_i_m', '$2a$10$h0FAhz1bvt5DmK/ZeZfzLeapGK6qJKpuPgt7nxXegepYhZwlzN8yy', 'ADMIN');
INSERT INTO users (email, first_name, last_name, login, password, role)
VALUES ('stazaev@mail.ru', 'Стазаев', 'Даниил', 'stazaev_d_s', '$2a$10$m5TyJ9Tg2Hd7t20V8i.1U./9OqoQiFdnqd3Zf5vxG0up7i7CQ9c8q', 'USER');
