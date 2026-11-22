# Інформаційна система лісових інспекцій

Курсова робота з дисципліни «Кросплатформне програмування», варіант 18.
Клієнт–серверна система обліку лісових ділянок, спостережень, порушень і завдань інспекції.
Для кожного порушення сервер автоматично обчислює інтегральний показник ризику та контролює
чотиристанний життєвий цикл порушення.

## Технології

- Java 21, Maven
- Сервер: Servlet API 4.0.1 (javax), Tomcat 9, JDBC, MySQL 8, Jackson 2.18.2
- Клієнт: JavaFX 21, `java.net.http.HttpClient`
- Тести: JUnit 5 (5.11.4), maven-surefire-plugin 3.5.2

## Структура репозиторію

```
database.sql            скрипт створення бази даних і початкових даних
docs/                   схеми (архітектура, ER-діаграма, життєвий цикл)
server/                 WAR-модуль: Servlet -> Service -> DAO -> JDBC -> MySQL
  src/main/java/...     моделі, DAO, Service, Servlet, RiskCalculator
  src/main/resources/   db.properties.example (шаблон налаштувань)
  src/test/java/...     модульні тести JUnit 5
client/                 JavaFX-клієнт
  src/main/java/...     MainApp, HttpForestClient, CsvObservationReader, CsvImportTask
  sample-data/          observations.csv і test-cases/ (файли для перевірки помилок імпорту)
```

Архітектура: `JavaFX -> HTTP/JSON -> Servlet -> Service -> DAO -> JDBC -> MySQL`.
Предметна валідація та робота з базою даних виконуються лише на сервері.

## Підготовка MySQL

1. Запустіть MySQL Server 8.
2. Виконайте скрипт `database.sql` (наприклад, у MySQL Workbench: File -> Open SQL Script -> Execute).
3. Скопіюйте `server/src/main/resources/db.properties.example` у `server/src/main/resources/db.properties`
   і вкажіть власні адресу, користувача та пароль. Файл `db.properties` не потрапляє до Git.

## Складання й запуск сервера

```
cd server
mvn clean package
```

1. Скопіюйте `server/target/forest-inspection-server.war` у каталог `webapps` Tomcat 9.
2. Запустіть Tomcat (`bin/catalina.bat run` у Windows, `bin/catalina.sh run` у Linux).
3. Перевірка: `http://localhost:8080/forest-inspection-server/plots` повинна повернути JSON.

## Запуск JavaFX-клієнта

Клієнт запускається з каталогу `client` (від цього залежить відносний шлях до CSV):

```
cd client
mvn javafx:run
```

Адресу сервера можна змінити параметром `-Dapi.base=http://хост:порт/forest-inspection-server`.

## Тести JUnit 5

Тести не потребують Tomcat, JavaFX і MySQL:

```
cd server
mvn clean test
```

Перевіряються: обчислення інтегрального показника та нормування факторів, відхилення недопустимих
значень, дозволені й заборонені переходи життєвого циклу, валідація лісових ділянок і спостережень.

## Імпорт спостережень із CSV

Кнопка «Імпортувати з CSV» у вкладці «Спостереження» читає файл `client/sample-data/observations.csv`
у фоновому потоці (JavaFX `Task`). Під час роботи показуються `ProgressBar` і текст стану, повторний
запуск заблоковано, імпорт можна скасувати. Після завершення таблиця перезавантажується із сервера.

Формат: UTF-8, роздільник `;`, десяткова крапка, обов'язковий заголовок:

```
plot_id;observed_at;observer;category;affected_area_ha;description
1;2026-11-10;Іваненко Петро;Самовільна рубка;1.2;Свіжі пеньки біля дороги
```

| Поле | Вимога |
|------|--------|
| plot_id | ціле число, ID наявної лісової ділянки |
| observed_at | дата РРРР-ММ-ДД |
| observer, category | непорожні рядки |
| affected_area_ha | число >= 0 |
| description | текст до 500 символів (може бути порожнім) |

Помилка файла або заголовка зупиняє імпорт до першого запиту. Некоректні рядки та відмови сервера
пропускаються з причиною в журналі й у підсумковому повідомленні. Записи, що вже є в базі, не
додаються вдруге (перевірка на дублікати). Приклади некоректних файлів — у `client/sample-data/test-cases/`.
