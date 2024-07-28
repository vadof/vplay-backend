# VCasino Documentation

- **Services**
- **Local environment setup**
- **Clicker**

# Local environment setup

Run `vcasino-backend/docker/postgres/docker-compose.yml`

# Services:

## User Service

**Задачи:**

- Регистрация и аутентификация пользователей.
- Хранение информации о пользователях.

**Необходимые компоненты:**

- Spring Security для аутентификации и авторизации.
- База данных (например, MySQL или PostgreSQL) для хранения данных пользователей.

## Game Service

**Задачи:**

-   Предостовление доступных игр (рулетка, слоты и т.д.).

## Betting Service

**Задачи:**

-   Обработка ставок пользователей.
-   Взаимодействие с wallet сервисом для обновления баланса.

**Необходимые компоненты:**

-   База данных для хранения информации о ставках.

## Wallet Service

**Задачи:**

-   Управление балансом пользователей.
-   Пополнение и вывод средств.
-   Ведение истории транзакций.

**Необходимые компоненты:**

-   Интеграция с платежными системами.
-   База данных для хранения информации о транзакциях.

## Notification Service

**Задачи:**

-   Отправка уведомлений пользователям (например, уведомления о выигрыше/проигрыше).
-   Поддержка различных каналов связи (email, SMS).

**Необходимые компоненты:**

-   Интеграция с внешними сервисами для отправки email и SMS.
-   Очереди сообщений (например, RabbitMQ или Kafka) для обработки уведомлений асинхронно.

## Analytics Service

**Задачи:**

-   Сбор и анализ данных о поведении пользователей и игре.
-   Создание отчетов и метрик для бизнеса.

**Необходимые компоненты:**

-   База данных для хранения аналитических данных.
-   Инструменты для визуализации данных (например, Grafana или Kibana).

## Admin Service

**Задачи:**

-   Управление и мониторинг системой.
-   Инструменты для модерации и поддержки пользователей.
-   Просмотр и управление данными всех остальных сервисов.

**Необходимые компоненты:**

-   Веб-интерфейс для администраторов.
-   Инструменты мониторинга (например, Spring Boot Admin).

## API Gateway

**Задачи:**

-   Централизованная точка входа для всех клиентов.
-   Маршрутизация запросов к соответствующим микросервисам.
-   Управление аутентификацией и авторизацией на уровне шлюза.

**Необходимые компоненты:**

-   Spring Cloud Gateway для маршрутизации и управления API.

## Eureka Service

**Задачи:**

-   Обнаружение и регистрация микросервисов.
-   Обеспечение динамической маршрутизации и балансировки нагрузки.

**Необходимые компоненты:**

-   Netflix Eureka или Consul для обнаружения сервисов.

## Circuit Breaker

**Задачи:**

-   Обеспечение отказоустойчивости и устойчивости системы при сбоях.
-   Управление отказами и восстановление после сбоев.

**Необходимые компоненты:**

-   Netflix Hystrix или Resilience4j для управления отказами.

## Logging and Monitoring Service


**Задачи:**

-   Сбор и анализ логов всех микросервисов.
-   Мониторинг состояния системы и выявление аномалий.

**Необходимые компоненты:**

-   ELK Stack (Elasticsearch, Logstash, Kibana) или Prometheus и Grafana для логирования и мониторинга.

# Clicker

User can change 1000 vcoins to 1$ <br>
User can change 1$ to 900 vcoins (10% commission) <br>
Last user level - 10 (Net worth: 1_000_000_000)

## Upgrades

Maximum possible earn per hour 1,000,000 vcoins -> 1,000$<br>
100/200/200/500 -> 1,000$

Price for upgrade is determined based on earn per hour
<i>profit_per_hour_delta</i> * <i>hours_to_payback</i><br>

| Section        | Level | Days to payback | Hours to payback |
|----------------|:-----:|:---------------:|:----------------:|
| Social         |  0-5  |        7        |       168        |
| Social         |  6-7  |       10        |       240        |
| Social         |  8-9  |       14        |       336        |
| Airlines       |  0-5  |        8        |       192        |
| Airlines       |  6-7  |       12        |       288        |
| Airlines       |  8-9  |       14        |       336        |
| Pharmaceutical |  0-5  |        8        |       192        |
| Pharmaceutical |  6-7  |       11        |       264        |
| Pharmaceutical |  8-9  |       15        |       360        |
| IT             |  0-4  |       10        |       240        |
| IT             |  5-7  |       14        |       336        |
| IT             |  8-9  |       21        |       504        |

**Social section maximum earn per hour:**

| Name      | Max level | Earn per hour |
|-----------|:---------:|:-------------:|
| X         |    10     |     2,000     |
| Reddit    |    10     |     4,000     |
| Snapchat  |    10     |     5,000     |
| Telegram  |    10     |     6,000     |
| TikTok    |    10     |     8,000     |
| WeChat    |    10     |    10,000     |
| Instagram |    10     |    12,000     |
| WhatsApp  |    10     |    15,000     |
| YouTube   |    10     |    18,000     |
| Facebook  |    10     |    20,000     |
| **Total** |           |    100,000    |

**Airlines section maximum earn per hour:**

| Name                   | Max level | Earn per hour |
|------------------------|:---------:|:-------------:|
| Lufthansa              |    10     |     4,000     |
| InterGlobe Aviation    |    10     |     7,000     |
| Singapore Airlines     |    10     |    10,000     |
| American Airlines      |    10     |    13,000     |
| China Eastern Airlines |    10     |    16,000     |
| China Western Airlines |    10     |    19,000     |
| Emirates               |    10     |    22,000     |
| Southwest Airlines     |    10     |    25,000     |
| Ryanair                |    10     |    29,000     |
| Delta Airlines         |    10     |    55,000     |
| **Total**              |           |    200,000    |

**Pharmaceutical section maximum earn per hour:**

| Name              | Max level | Earn per hour |
|-------------------|:---------:|:-------------:|
| AstraZeneca       |    10     |     3,000     |
| Sanofi            |    10     |     6,000     |
| Merck & Co.       |    10     |     9,000     |
| Bayer             |    10     |    12,000     |
| Novartis          |    10     |    15,000     |
| AbbVie            |    10     |    18,000     |
| Roche Holding     |    10     |    21,000     |
| Sinopharm         |    10     |    24,000     |
| Pfizer            |    10     |    30,000     |
| Johnson & Johnson |    10     |    62,000     |
| **Total**         |           |    200,000    |

**IT section maximum earn per hour:**

| Name      | Max level | Earn per hour |
|-----------|:---------:|:-------------:|
| Adobe     |    10     |     5,000     |
| Netflix   |    10     |    10,000     |
| Samsung   |    10     |    20,000     |
| Tesla     |    10     |    30,000     |
| Meta      |    10     |    40,000     |
| Amazon    |    10     |    50,000     |
| Google    |    10     |    60,000     |
| NVIDIA    |    10     |    70,000     |
| Microsoft |    10     |    80,000     |
| Apple     |    10     |    135,000    |
| **Total** |           |    500,000    |
