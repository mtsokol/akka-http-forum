CREATE TABLE users
(
  id       SERIAL       NOT NULL
    CONSTRAINT users_pkey
    PRIMARY KEY,
  nickname VARCHAR(255) NOT NULL,
  email    VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX users_id_uindex
  ON users (id);

CREATE TABLE topics
(
  id        SERIAL                     NOT NULL
    CONSTRAINT topics_pkey
    PRIMARY KEY,
  user_id   INTEGER                    NOT NULL
    CONSTRAINT topics_users_id_fk
    REFERENCES users
    ON UPDATE CASCADE ON DELETE CASCADE,
  timestamp VARCHAR(255) DEFAULT now() NOT NULL,
  secret    VARCHAR(255)               NOT NULL,
  subject   VARCHAR(255)               NOT NULL,
  content   VARCHAR(1024)
);

CREATE UNIQUE INDEX topics_id_uindex
  ON topics (id);

CREATE TABLE answers
(
  id        SERIAL                     NOT NULL
    CONSTRAINT answers_pkey
    PRIMARY KEY,
  user_id   INTEGER                    NOT NULL
    CONSTRAINT answers_users_id_fk
    REFERENCES users
    ON UPDATE CASCADE ON DELETE CASCADE,
  topic_id  INTEGER                    NOT NULL
    CONSTRAINT answers_topics_id_fk
    REFERENCES topics
    ON UPDATE CASCADE ON DELETE CASCADE,
  timestamp VARCHAR(255) DEFAULT now() NOT NULL,
  secret    VARCHAR(255)               NOT NULL,
  content   VARCHAR(1024)              NOT NULL
);

CREATE UNIQUE INDEX answers_id_uindex
  ON answers (id);

CREATE VIEW popular AS
  SELECT
    t.id,
    t.subject,
    (SELECT a."timestamp"
     FROM (answers a
       JOIN topics t2 ON ((a.topic_id = t2.id)))
     WHERE (t.id = t2.id)
     GROUP BY t2.id, a."timestamp"
     ORDER BY a."timestamp" DESC
     LIMIT 1) AS a_time,
    users.nickname,
    t."timestamp"
  FROM (topics t
    JOIN users ON ((t.user_id = users.id)))
  ORDER BY (SELECT a."timestamp"
            FROM (answers a
              JOIN topics t2 ON ((a.topic_id = t2.id)))
            WHERE (t.id = t2.id)
            GROUP BY t2.id, a."timestamp"
            ORDER BY a."timestamp" DESC
            LIMIT 1) DESC NULLS LAST;

