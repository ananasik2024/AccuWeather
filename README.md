# AccuWeather API Tests (Rest Assured + JUnit 5)

Учебный проект по автоматизации тестирования API.

## Описание
- Проект создан для тестирования публичного API [AccuWeather](https://developer.accuweather.com/).
- Использован **Java 17 + Maven + Rest Assured + JUnit 5**.
- Покрыто **20 тестов** для различных эндпоинтов:
    - Locations (поиск городов, автодополнение, geoposition, индексы и т.д.)
    - Current Conditions (текущая погода, история 6h/24h, топ 50 городов)
    - Forecasts (прогнозы: 1 день, 5 дней, 12h, 24h, quarter-day)
    - Indices (1 день, 5 дней)
    - Alerts (погодные предупреждения)
- Тесты проверяют статус-коды, заголовки, время ответа (< 3s), контент в JSON.
- **Добавлены мок-тесты с WireMock** — все 20 запросов проверяются офлайн с использованием фикстур JSON.
- **Логирование запросов/ответов** — через `RequestLoggingFilter` и `ResponseLoggingFilter`, выводится в консоль при запуске тестов.

## Подготовка
1. Зарегистрироваться на [AccuWeather Developer Portal](https://developer.accuweather.com/).
2. Создать приложение (My Apps) и получить API_KEY.
3. Указать ключ в файле:

src/test/resources/config.properties
   
API_KEY=YOUR_KEY

## Запуск
В терминале в корне проекта:
```bash
mvn test
```
Или через IntelliJ IDEA — Run AccuWeatherApiTests.

## Примечания
- Для бесплатного тарифа часть эндпоинтов может возвращать 401/403.
- В тестах это учтено: допустимы коды 200/401/403 (а для Alerts — ещё и 204).
