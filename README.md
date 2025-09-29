# ATM Incidents Analyzer
Ремонт АТМ. Анализ причины и сроков ремонта

Веб-приложение для анализа данных о ремонтах банкоматов с возможностью загрузки данных из Excel-файлов и различными видами аналитики.

## Функциональность

### Основные возможности:
- **Загрузка данных** из XLS/XLSX файлов
- **Просмотр всех данных** о ремонтах в табличном виде
- **Аналитика**:
  - Топ наиболее частых причин неисправностей
  - Топ самых долгих ремонтов
  - Анализ повторяющихся поломок в течение заданного периода
- **Редактирование данных** прямо в интерфейсе
- **Управление данными** (удаление всех записей)

### Технические особенности:
- **Backend**: Spring Boot приложение с REST API
- **Frontend**: Vue.js с адаптивным дизайном
- **База данных**: SQLite с Hibernate
- **Файловая обработка**: Apache POI для работы с Excel

## Структура проекта
```
src/
├── main/
│ ├── java/com/example/atmra/
│ │ ├── controller/ # REST контроллеры
│ │ ├── entity/ # JPA сущности
│ │ ├── dto/ # Data Transfer Objects
│ │ ├── repository/ # JPA репозитории
│ │ ├── service/ # Бизнес-логика
│ │ └── mapper/ # MapStruct мапперы
│ ├── resources/
│ │ ├── static/ # Статические ресурсы (CSS, JS)
│ │ └── templates/ # HTML шаблоны
│ └── application.properties # Конфигурация
```

## API Endpoints

### Основные endpoints:
- `GET /api/incidents/data/{type}` - получение данных по типу
- `POST /api/incidents/update` - обновление записи
- `POST /api/incidents/upload` - загрузка файла
- `GET /api/incidents/deleteAll` - удаление всех данных
- `GET /config.js` - конфигурация для фронтенда

### Типы данных:
- `allData` - все данные
- `mostCommonCauses` - частые причины
- `longestRepairTimes` - долгие ремонты
- `causeFailureRecurred` - повторные поломки

## Требования

### Системные требования:
- Java 21+
- Maven 3.6+
- Современный браузер с поддержкой ES6+

### Зависимости:
- Spring Boot 3.x
- SQLite
- Hibernate
- Apache POI
- MapStruct
- Lombok
- Vue.js 3

## Установка и запуск

### Клонирование репозитория
```bash
git clone <repository-url>
cd atm-incidents-analyzer
```
### Сборка проекта
```bash
mvn clean package
```
### Запуск приложения из Maven
```bash
mvn spring-boot:run
```
### Запуск приложения jar
```bash
java -jar atm-repairs-analyzer-0.0.1-SNAPSHOT.jar
```

### Доступ к приложению
```bash
Откройте в браузере: http://localhost:8080
```

## Конфигурация
### Настройки в application.properties:
#### Количество записей для аналитики
```bash
atm-repairs-analizer.count-top-most-common-causes=3
atm-repairs-analizer.count-longest-repair-times=3
atm-repairs-analizer.count-cause-failure-recurred=15
```
#### Порт сервера
```bash
server.port=8080
```
### Настройки базы данных в application.yml:
```yaml
spring:
  datasource:
    url: jdbc:sqlite:db.sqlite
  jpa:
    hibernate:
      ddl-auto: update
```
