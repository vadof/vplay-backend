# VCasino Documentation

- **[Local environment setup](#local-environment-setup)**
- **[Services](#services)**

# Local environment setup

For Dev environment run `vcasino-backend/docker/dev/docker-compose.yml` <br>
For Test environment run `vcasino-backend/docker/test/docker-compose.yml`

# Services:

- [API Gateway](#api-gateway)
- [Eureka Service](#eureka-service)
- [User Service](#user-service)
- [Clicker service](#clicker-service)
- [Game Service](#game-service)
- [Betting Service](#betting-service)
- [Wallet Service](#wallet-service)
- [Notification Service](#notification-service)
- [Analytics Service](#analytics-service)
- [Admin Service](#admin-service)
- [Circuit Breaker](#circuit-breaker)
- [Logging and Monitoring Service](#logging-and-monitoring-service)

## API Gateway

**Description**

- Централизованная точка входа для всех клиентов.
- Маршрутизация запросов к соответствующим микросервисам.
- Управление аутентификацией и авторизацией на уровне шлюза.

**Components**

- Spring Cloud Gateway для маршрутизации и управления API.

## Eureka Service

**Description**

- Обнаружение и регистрация микросервисов.
- Обеспечение динамической маршрутизации и балансировки нагрузки.

**Components**

- Netflix Eureka

## User Service

**Description**

- Регистрация и аутентификация пользователей.
- Хранение информации о пользователях.

**Components**

- Spring Security для аутентификации и авторизации.
- База данных PostgreSQL для хранения данных пользователей.

## Clicker service

**Description**

- The main way to earn game currency (vcoins) which in the future can be exchanged for $ to user's wallet

**Estimated calculations**

| Level | Earn per tap | Energy restore per sec | Max energy |   Net worth   | Days to achieve |
|-------|:------------:|:----------------------:|:----------:|:-------------:|:---------------:|
| 1     |      1       |           1            |    1000    |       0       |        0        |
| 2     |      2       |           2            |    2000    |    10_000     |        0        |
| 3     |      3       |           3            |    3000    |    25_000     |        0        |
| 4     |      4       |           4            |    4000    |    100_000    |        1        |
| 5     |      5       |           5            |    5000    |   1_000_000   |        6        |
| 6     |      6       |           6            |    6000    |   2_000_000   |        9        |
| 7     |      7       |           7            |    7000    |  10_000_000   |       23        |
| 8     |      8       |           8            |    8000    |  50_000_000   |       58        |
| 9     |      9       |           9            |    9000    |  100_000_000  |       63        |
| 10    |      10      |           10           |   10000    | 1_000_000_000 |       100       |

| Level | Earn for taps (h) | Passive earn (h) | Total (h) | Earn for taps (d) | Passive earn (d) | Total (d)  |
|-------|:-----------------:|:----------------:|:---------:|:-----------------:|:----------------:|:----------:|
| 1     |       3_600       |        0         |   3_600   |      50_000       |        0         |   50_000   |
| 2     |       7_200       |        20        |   7_220   |      100_000      |       480        |  100_480   |
| 3     |      10_800       |       100        |  10_900   |      150_000      |      2_400       |  152_400   |
| 4     |      14_400       |       500        |  14_900   |      200_000      |      12_000      |  212_000   |
| 5     |      18_000       |      5_500       |  23_500   |      250_000      |     132_000      |  382_000   |
| 6     |      21_600       |      11_000      |  32_600   |      300_000      |     264_000      |  564_000   |
| 7     |      25_200       |      50_000      |  75_200   |      350_000      |    1_200_000     | 1_550_000  |
| 8     |      28_800       |     225_000      |  253_800  |      400_000      |    5_400_000     | 5_800_000  |
| 9     |      32_400       |     400_000      |  432_400  |      450_000      |    9_600_000     | 10_050_000 |
| 10    |      36_000       |    1_000_000     | 1_003_600 |      500_000      |    24_000_000    | 24_500_000 |

**Admin functionality**

- Можно добавить vcoins пользователю
- Статистика сколько пользователей забрало различных наград за определенный день

**App Sections**

- [Common](#common)
- [Tap](#tap-section)
- [Upgrade](#upgrade-section)
- [Friends](#friends-section)
- [Rewards](#rewards-section)
- [Top](#top-section)

### Common

- Видна почти во всех секциях
- текущий баланс
- заработок в час
- Текущий уровень пользователя на который он может нажать и посмотреть информацию о всех уровнях <br>
  Last user level = 10 (Net worth: 1_000_000_000)
- Окно настроек где пользователь может ? (TODO)
    - User can exchange 100_000 vcoins for 1$ to his wallet <br>

### Tap section

**TapToEarn = 1-10**<br>
**Energy = 100-1000 (TODO clarify)**

- Основная механика зарабатывания vcoins - tap to earn
- У пользователя есть определенное количество энергии 1 energy = 1 EarnPerTap
- For each level, the user's earnings per tap improve. EarnPerTap = lvl
- В секунду регенируется определенное количество энергии (3)
- The user can improve the maximum energy reserve, max value = ?.
- There is a boost tab where you can replenish all your energy ? times a day

### Upgrade section

Улучшения отвечают за пассивный доход <br>
Maximum possible earn per hour 1,000,000 vcoins -> 1,000$<br>
100/200/200/500 -> 100$

Price for upgrade is determined based on upgrade's (<i>profit_per_hour_delta</i> * <i>hours_to_payback</i>)<br>

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

### Friends section

- The user can invite friends, when going through the referral system, the user receives a bonus of ? vcoins
- If a user's friend levels up, the user gets a reward for that vcoins * lvl
- The user can add friends (for top tab)

### Rewards section

- Every day, when logging in, the user can pick up a daily bonus. 1-10 days. After picking up the daily 10th prize, the
  counter is reset.
- Every day, a new video appears, upon viewing which the user can receive a reward.

### Top section

- Пользователь может посмотреть топ по миру
- Пользователь может посмотреть топ среди друзей
- Топ обновляется каждый день в 00:00 по UTC

## Game Service

**Description**

- Предостовление доступных игр (рулетка, слоты и т.д.).

## Betting Service

**Description**

- Обработка ставок пользователей.
- Взаимодействие с wallet сервисом для обновления баланса.

## Wallet Service

**Description**

- Управление балансом пользователей.
- Пополнение и вывод средств.
- Ведение истории транзакций.

**Components**

- Интеграция с платежными системами.
- База данных для хранения информации о транзакциях.

**Feautures**

- User can change 1$ to 9000 vcoins (10% commission)

## Notification Service

**Description:**

- Отправка уведомлений пользователям (например, уведомления о выигрыше/проигрыше).
- Поддержка различных каналов связи (email, SMS).

**Components**

- Интеграция с внешними сервисами для отправки email и SMS.
- Очереди сообщений (например, RabbitMQ или Kafka) для обработки уведомлений асинхронно.

## Analytics Service

**Description**

- Сбор и анализ данных о поведении пользователей и игре.
- Создание отчетов и метрик для бизнеса.

**Components**

- База данных для хранения аналитических данных.
- Инструменты для визуализации данных (например, Grafana или Kibana).

## Admin Service

**Description**

- Управление и мониторинг системой.
- Инструменты для модерации и поддержки пользователей.
- Просмотр и управление данными всех остальных сервисов.

**Components**

- Веб-интерфейс для администраторов.
- Инструменты мониторинга (например, Spring Boot Admin).

## Circuit Breaker

**Description**

- Обеспечение отказоустойчивости и устойчивости системы при сбоях.
- Управление отказами и восстановление после сбоев.

**Components**

- Netflix Hystrix или Resilience4j для управления отказами.

## Logging and Monitoring Service

**Description**

- Сбор и анализ логов всех микросервисов.
- Мониторинг состояния системы и выявление аномалий.

**Components**

- ELK Stack (Elasticsearch, Logstash, Kibana) или Prometheus и Grafana для логирования и мониторинга.
