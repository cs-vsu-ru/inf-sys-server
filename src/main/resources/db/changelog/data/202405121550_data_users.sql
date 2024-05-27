--liquibase formatted sql

--changeset tukitoki:202405121550_data_users context:local

-- users data

INSERT INTO users (id, login, password, first_name, last_name, email, role)

VALUES (1,'borisov','$2a$10$2dXZwrlM4E1OFYy7ZIGhN.ueH6H/wYtGa9PcK7W3bvhN6OGB8GIFC','Дмитрий','Борисов','borisov@sc.vsu.ru','ADMIN'),
(31,'desyatirikova','$2a$10$jvf.R.wSgvcB.N1LM3dhqOE72MXFfmlB5MQNUWDqMR.uNnNqbATAa','Елена','Десятирикова','science2000@yandex.ru','USER'),
(32,'ermakov','$2a$10$5422ilBKtAeezyAPMwYx0uyM9GfrbBcQme.cKqiMsh5Y379b1LujC','Михаил','Ермаков','micermakov@yandex.ru','USER'),
(33,'zuyev','$2a$10$dZ2JsJd8GxzHM82jhc5Mq.kAg1ng3fPzNevjiIV3ns6RZm9y0Cu52','Сергей','Зуев','zsa_zuev@mail.ru','USER'),
(34,'koval','$2a$10$7wrtwctbL63wGqxCZvRv6ulQVkHrAgdHNiP3USKFE9V7Tj9kHnqC2','Андрей','Коваль','koval@sc.vsu.ru','USER'),
(35,'malykhin','$2a$10$FQ.6/TqU9f30.W60CZ17wOEEwwOEPlDa7gIYuzomwCu4rp2AHikKK','Андрей','Малыхин','mal_and@inbox.ru','USER'),
(36,'mahortov','$2a$10$UT6GuVdw/9ejbWpJVzpTI.VorfwlfbIZLTvAvoSFvjWmc4oHCuF1a','Сергей','Махортов','mahortov@sc.vsu.ru','USER'),
(37,'evpopova','$2a$10$UXoiunDEHvfPGBF3isK9eOvYe7.l4Mi5zaj5EiqpHqKotmxiBKZM2','Елена','Попова','popova_e_v@sc.vsu.ru','USER'),
(38,'savinkov_a_yu','$2a$10$Vdr01NDr1d1R34hV08sRbeGjOZjc9A35Kg6xQX5x4jVIYZB9iwg7q','Андрей','Савинков','a.savinkov@gmail.com','USER'),
(39,'assamadurov','$2a$10$LgIvW6J0FqalrVljk.ROQOtbCuxWgc1Wpvj.U01Q2JAJDrh5jJRzm','Александр','Самодуров','unaxel2000@mail.ru','USER'),
(40,'stromov_a_v','$2a$10$c60x1PXqyDujRJOCjgfu0uLfnEGnOrICTCJqSu5sRKkbsxv8YaytW','Александр','Стромов','astromov@yandex.ru','USER'),
(41,'sav','$2a$10$/3X.dWRET2lzwucigf.g8O9DI/qAzwzTcDp5SyrI5QB0QGhCTk.Gm','Александр','Сычев','sav@sc.vsu.ru','USER'),
(42,'tap','$2a$10$ESapxouT2CdmyGfvdINUuOzgoxnKtsyQYcsLdRbgUPb01JvugINPm','Александр','Толстобров','tap@vsu.ru','USER'),
(43,'fvv','$2a$10$qPKDcM.Elr4DnW0mG7Qxje9UonpDXjfTR8d9M0PyEpAsbwBVDMm0q','Вадим','Фертиков','fvv@cs.vsu.ru','USER'),
(44,'chernitsin','$2a$10$eDRgjUS2PYEecYVBkVwRwOo7NvMXv.Gp.Dbbu0T70lY4KKSXFKcDm','Дмитрий','Черницын','cdv-cs@yandex.ru','USER'),
(45,'romancova','$2a$10$/FJzQ2fz1xzbIXamHj.a0e4tALwa8ByHFVZYQQisK3esOF7bbU92S','Татьяна','Романцова','rom@cs.vsu.ru','MODERATOR'),
(46,'borisova_a_a','$2a$10$KoT4Ctc7aheAEmBZFDH0TOQVOSNtm08qNkzm41pUqJREpm4xSYicS','Алла','Борисова','borisova_a_a@sc.vsu.ru','MODERATOR'),
(47,'starodubcev_i_yu','$2a$10$l0MNjIWLLLKvRdwbTbSUzeAS6zQitAXz/wWdrTCnNft/RStU07L8C','Игорь','Стародубцев','starodubcev_i_yu@sc.vsu.ru','USER'),
(48,'makovij_k_a','$2a$10$542HhF8LhKCtSqF.jtZfV.pkTKoZmsuuhIJhRADIEoeobdQx18GCi','Катерина','Маковий','makovij_k_a@sc.vsu.ru','USER'),
(49,'podoprikhina_l_o','$2a$10$19XjGQXNHI7/lqWTnQzBcex5jDYX5DuXsMI1YE0u0HR9.qLmcz4zi','Лилия','Подоприхина','podoprikhina_l_o@sc.vsu.ru','USER'),
(50,'abramov_g_v','$2a$10$bk90YNR/.HVG0Pwgb.bSCu7QLOqC.ub8akmEbxvzF9AH.W5ZES4US','Геннадий','Абрамов','agwl@yandex.ru','USER'),
(51,'chernyshov_m_k','$2a$10$NHP55w/WTU/s2NZr1ww2EeoWHPi27S82qDKGOaesKD0uJhTDSw2Hq','Максим','Чернышов','mkch69@gmail.com','USER'),
(52,'kudinov_i_m','$2a$10$XCQ04cp8J2s9Qvuc2YEjF.bSemexiP.deJ0wFsAtPt09Z1dldWWza','Иван','Кудинов','kudinov_im@cs.vsu.ru','ADMIN');

SELECT setval('users_sequence', 52, true);