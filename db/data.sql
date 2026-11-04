-- Тестовые данные. Выполнять сразу после schema.sql (на пустых таблицах),
-- тогда id получаются по порядку: 1, 2, 3...

SET ROLE survey_owner;

-- id 1..3
-- id 1..3 - обычные пользователи, id 4 - администратор.
-- Пароли (только для тестового стенда!):
--   ivan / Ivan2026!    maria / Maria2026!    oleg / Oleg2026!    admin / Admin2026!
-- В БД хранятся только BCrypt-хэши.
INSERT INTO app.users (username, password_hash, role, status, name, email)
VALUES ('ivan',  '$2a$10$v3SuR6CCiqBeGXWwbJ56juG2bCYiaO08DhsEaFXfQlj.6neVCZANO', 'USER',  'ACTIVE', 'Иван Петров',    'ivan@example.com'),
       ('maria', '$2a$10$v4ZWoHOaTafZRVD.QINy6eUrOXJYZhuLsPi9XeMQkNBWlQ7TRs03G', 'USER',  'ACTIVE', 'Мария Смирнова', 'maria@example.com'),
       ('oleg',  '$2a$10$nkdQB/GUetoRG4auJG22ZeW6c0iCQQMPWdnGEgtJ5DMqp9HM/771.', 'USER',  'ACTIVE', 'Олег Кузнецов',  'oleg@example.com'),
       ('admin', '$2a$10$6DASLvA7U9SiHS4.gUdPuuwcnB.ifyD6Low/4jcKhYAB29hPLzr5m', 'ADMIN', 'ACTIVE', 'Администратор',  'admin@example.com');

-- 1: активный опрос Ивана, 2: черновик Ивана, 3: черновик Марии без вопросов
INSERT INTO app.surveys (title, author_id, starts_at, ends_at, status)
VALUES ('Качество столовой', 1, now() - interval '1 day', now() + interval '30 days', 'ACTIVE'),
       ('Учебные курсы',     1, NULL, now() + interval '60 days', 'DRAFT'),
       ('Пустой черновик',   2, NULL, NULL, 'DRAFT');

-- вопросы 1..2 в опросе 1, вопрос 3 в опросе 2
INSERT INTO app.survey_questions (survey_id, text, required)
VALUES (1, 'Нравится ли вам еда?', TRUE),
       (1, 'Как часто вы обедаете в столовой?', FALSE),
       (2, 'Какой курс полезнее всего?', TRUE);

INSERT INTO app.question_options (question_id, position, value)
VALUES (1, 0, 'Да'), (1, 1, 'Нет'), (1, 2, 'Иногда'),
       (2, 0, 'Каждый день'), (2, 1, 'Раз в неделю'), (2, 2, 'Никогда'),
       (3, 0, 'Java'), (3, 1, 'Сети'), (3, 2, 'Криптография');

-- участие 1: Мария отправила; участие 2: Олег в процессе
INSERT INTO app.survey_participations (survey_id, respondent_id, status, submitted_at)
VALUES (1, 2, 'SUBMITTED', now()),
       (1, 3, 'IN_PROGRESS', NULL);

INSERT INTO app.survey_answers (participation_id, question_id, value)
VALUES (1, 1, 'Да'),
       (1, 2, 'Каждый день'),
       (2, 2, 'Раз в неделю');

RESET ROLE;