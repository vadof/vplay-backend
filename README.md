# Documentation

- **[Local environment setup](#local-environment-setup)**
- **[Services](#services)**

# Local environment setup

For Dev environment run `vcasino-backend/docker/dev/docker-compose.yml` <br>
For Test environment run `vcasino-backend/docker/test/docker-compose.yml`

Environment variables
```
YOUTUBE_API_KEY (key to interact with youtube videos)
GOOGLE_OAUTH_CLIENT_ID (Google OAuth2)
GOOGLE_OAUTH_CLIENT_SECRET (Google OAuth2)
GITHUB_OAUTH_CLIENT_ID (GitHub OAuth2)
GITHUB_OAUTH_CLIENT_SECRET (GitHub OAuth2)
FACEBOOK_OAUTH_CLIENT_ID (Facebook OAuth2)
FACEBOOK_OAUTH_CLIENT_SECRET (Facebook OAuth2)
DISCORD_OAUTH_CLIENT_ID (Discord OAuth2)
DISCORD_OAUTH_CLIENT_SECRET (Discord OAuth2)
CLIENT_URL=http://localhost:4200
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=86400000
CONFIRMATION_TOKEN_EXPIRATION_MS=3600000
KEYS_PATH (absolute path to folder with public.key/private.key for jwt auth)
PUBLIC_KEY_PATH (absolute path to public.key)
PRODUCTION=false
```

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

- Centralized entry point for all clients.
- Routing requests to the appropriate microservices.
- Gateway-level authentication and authorization management.

**Components**

- Spring Cloud Gateway for API routing and management.

## Eureka Service

**Description**

- Discovery and registration of microservices.
- Providing dynamic routing and load balancing.

**Components**

- Netflix Eureka

## User Service

**Description**

- Registration and authentication of users.
- Storage of information about users.

**Components**

- Spring Security for authentication and authorization.
- PostgreSQL database for storing user data.

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

- Add vcoins to a user
- Statistics on how many users have taken various rewards for a certain day
- The admin can add a video for viewing which the user receives a reward. (input=link,reward,date)

**App Sections**

- [Common](#common)
- [Tap](#tap-section)
- [Upgrade](#upgrade-section)
- [Friends](#friends-section)
- [Rewards](#rewards-section)
- [Company](#company-section)
- [Top](#top-section)

### Common

- Visible in almost all sections
- current balance
- earnings per hour
- Current user level which he can click on and see information about all levels <br>
  Last user level = 10 (Net worth: 1_000_000_000)
- Settings window where the user can? (TODO)
- User can exchange 100_000 vcoins for 1$ to his wallet <br>

### Tap section

**TapToEarn = 1-10**<br>
**Energy = 1000-10000**

- The main mechanics of earning vcoins is tap to earn
- The user has a certain amount of energy 1 energy = 1 EarnPerTap
- For each level, the user's earnings per tap improve. EarnPerTap = lvl
- A certain amount of energy is regenerated per second (3)
- The user can improve the maximum energy reserve, max value = ?.
- There is a boost tab where you can replenish all your energy ? times a day

### Upgrade section

Upgrades are responsible for passive income <br>
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

### Company section

**Description**
- The user will have a choice to create their own company or get a job in a company
- Players can apply for jobs in companies (optional can specify the desired fixed salary and % of revenue), and the owners (or managers) accept them.
* To create a company you need:
  - Company name
  - Unique company code
  - Description
  - Start-up capital (min 5M)
  - Minimum number of clicks per week (optional)
- Maximum number of employees = 10
- Maximum number of employees can be improved
- Each employee in the company has a role (custom ones can be made)
- Company reports are updated every day
- The company can go bankrupt if the company's capital falls to 0
- Share system (???)
  (Storing daily report in json structure)



|             | Earn (d) | Salary (fixed/%) | Company income (50%) | Employee salary |
|-------------|:--------:|:----------------:|:--------------------:|:---------------:|
| Employee 1  |   20K    |      2K/5%       |         10K          |    2K+1K=3K     |
| Employee 2  |   20K    |      10K/5%      |         10K          |   10K+1K=11K    |
| Employee 3  |   20K    |      10K/5%      |         10K          |   10K+1K=11K    |
| Employee 4  |   100K   |     10K/10%      |         50K          |   10K+10K=20K   |
| Employee 5  |   100K   |     10K/10%      |         50K          |   10K+10K=20K   |
| Employee 6  |   100K   |     10K/10%      |         50K          |   10K+10K=20K   |
| Employee 7  |   200K   |     20K/20%      |         100K         |   20K+40K=60K   |
| Employee 8  |   200K   |     20K/20%      |         100K         |   20K+40K=60K   |
| Employee 9  |   200K   |     20K/20%      |         100K         |   20K+40K=60K   |
| Employee 10 |   200K   |     20K/20%      |         100K         |   20K+40K=60K   |
| Total       |          |                  |         580K         |      294K       |

**Income tax (10%)**: (Company income - Employee salary) * Tax<br>
**Income tax (10%)**: (580K - 294K) * 0.1 = 28.6K<br>
**Day tax**: Number of employees * 5K<br>
**Day tax**: 10 * 5K = 50K<br>
**Net profit**: Company income - Employee salary - Income tax - Day tax<br>
**Net profit**: 580K - 294K - 28.6K - 50K = 207,4K<br>

### Top section

- User can see top in the world
- User can see top among friends
- Top is updated every day at 00:00 UTC

## Game Service

**Description**

- Provision of available games (roulette, slots, etc.).

## Betting Service

**Description**

- Processing user bets.
- Interaction with the wallet service to update the balance.

## Wallet Service

**Description**

- Managing user balances.
- Depositing and withdrawing funds.
- Maintaining transaction history.

**Components**

- Integration with payment systems.
- Database for storing transaction information.

**Feautures**

- User can change 1$ to 9000 vcoins (10% commission)

## Notification Service

**Description:**

- Sending notifications to users (e.g. notifications about winning/losing).
- Support for various communication channels (email, SMS).

**Components**

- Integration with external services for sending email and SMS.
- Message queues (e.g. RabbitMQ or Kafka) for processing notifications asynchronously.

## Analytics Service

**Description**

- Collection and analysis of user behavior and game data.
- Creation of reports and metrics for business.

**Components**

- Database for storing analytical data.
- Tools for data visualization (e.g. Grafana or Kibana).

## Admin Service

**Description**

- System management and monitoring.
- Tools for moderation and user support.
- Viewing and managing data from all other services.

**Components**

- Web interface for administrators.
- Monitoring tools (e.g. Spring Boot Admin).

## Circuit Breaker

**Description**

- Ensuring fault tolerance and system resilience during failures.
- Failure management and recovery from failures.

**Components**

- Netflix Hystrix or Resilience4j for failure management.

## Logging and Monitoring Service

**Description**

- Collection and analysis of logs of all microservices.
- Monitoring the system status and identifying anomalies.

**Components**

- ELK Stack (Elasticsearch, Logstash, Kibana) or Prometheus and Grafana for logging and monitoring.
