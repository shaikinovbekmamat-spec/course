-- V1__init_schema.sql
-- Initial database schema for Online Course Platform

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    slug    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE courses (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    instructor_id BIGINT       NOT NULL REFERENCES users(id),
    category_id   BIGINT       REFERENCES categories(id),
    published     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE lessons (
    id           BIGSERIAL PRIMARY KEY,
    course_id    BIGINT       NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    content      TEXT,
    order_index  INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE enrollments (
    id           BIGSERIAL PRIMARY KEY,
    student_id   BIGINT    NOT NULL REFERENCES users(id),
    course_id    BIGINT    NOT NULL REFERENCES courses(id),
    enrolled_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_enrollment UNIQUE (student_id, course_id)
);

CREATE TABLE lesson_progress (
    id             BIGSERIAL PRIMARY KEY,
    enrollment_id  BIGINT    NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id      BIGINT    NOT NULL REFERENCES lessons(id)     ON DELETE CASCADE,
    completed_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_lesson_progress UNIQUE (enrollment_id, lesson_id)
);

-- Indexes for performance
CREATE INDEX idx_courses_instructor  ON courses(instructor_id);
CREATE INDEX idx_courses_category    ON courses(category_id);
CREATE INDEX idx_courses_published   ON courses(published);
CREATE INDEX idx_lessons_course      ON lessons(course_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course  ON enrollments(course_id);
