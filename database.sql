DROP DATABASE IF EXISTS forest_inspection;
CREATE DATABASE forest_inspection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE forest_inspection;

CREATE TABLE forest_plot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    forest_type VARCHAR(80) NOT NULL,
    area_ha DOUBLE NOT NULL,
    sensitivity_level INT NOT NULL,
    CHECK (area_ha > 0),
    CHECK (sensitivity_level BETWEEN 1 AND 5)
);

CREATE TABLE observation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plot_id BIGINT NOT NULL,
    observed_at DATE NOT NULL,
    observer VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    affected_area_ha DOUBLE NOT NULL,
    description VARCHAR(500),
    CHECK (affected_area_ha >= 0),
    FOREIGN KEY (plot_id) REFERENCES forest_plot(id)
);

CREATE TABLE violation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    observation_id BIGINT NOT NULL,
    severity_level INT NOT NULL,
    state VARCHAR(30) NOT NULL,
    f1 DOUBLE NOT NULL,
    f2 DOUBLE NOT NULL,
    f3 DOUBLE NOT NULL,
    integral_index DOUBLE NOT NULL,
    CHECK (severity_level BETWEEN 1 AND 5),
    FOREIGN KEY (observation_id) REFERENCES observation(id)
);

CREATE TABLE inspection_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    violation_id BIGINT NOT NULL,
    inspector VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    planned_date DATE,
    is_done BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (violation_id) REFERENCES violation(id)
);

INSERT INTO forest_plot (code, name, forest_type, area_ha, sensitivity_level) VALUES
('LP-001', 'Полтавське лісництво, квартал 12', 'Сосновий бір', 120.5, 4),
('LP-002', 'Решетилівське лісництво, квартал 7', 'Дубовий ліс', 85.0, 5),
('LP-003', 'Лубенське лісництво, квартал 21', 'Мішаний ліс', 210.3, 2);

INSERT INTO observation (plot_id, observed_at, observer, category, affected_area_ha, description) VALUES
(1, '2026-10-02', 'Іваненко О. П.', 'Незаконна рубка', 2.5, 'Виявлено свіжі пні та сліди техніки'),
(2, '2026-10-05', 'Петренко М. І.', 'Стихійне сміттєзвалище', 0.4, 'Побутові відходи біля лісової дороги'),
(3, '2026-10-09', 'Коваль Н. В.', 'Пошкодження шкідниками', 6.0, 'Осередок короїда-типографа'),
(1, '2026-10-12', 'Іваненко О. П.', 'Пожежа', 12.0, 'Низова пожежа, площа уточнюється');

INSERT INTO violation (observation_id, severity_level, state, f1, f2, f3, integral_index) VALUES
(1, 4, 'DETECTED', 25.0, 80.0, 80.0, 58.0),
(2, 2, 'VERIFIED', 4.0, 40.0, 100.0, 40.6),
(4, 5, 'ACTION_PLANNED', 100.0, 100.0, 80.0, 95.0);

INSERT INTO inspection_task (violation_id, inspector, description, planned_date, is_done) VALUES
(3, 'Мельник Т. Г.', 'Обстеження території пожежі та складання акта', '2026-11-20', 0),
(2, 'Шевченко Р. А.', 'Контрольна перевірка очищення території', '2026-11-25', 0);
