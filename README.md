# Challenge Técnico - Microservicios Forex

## Estructura del Proyecto

```
prueba-tec-sprinboot/
├── forex-api/           # Microservicio 1 - API de tasas de cambio
│   └── Puerto: 8080
│
└── history-api/         # Microservicio 2 - Historial de conversiones (H2)
    └── Puerto: 8081
```

## Tecnologías Usadas

| Requisito | Implementado |
|-----------|-------------|
| Spring Boot 3.x + Java 17 | ✅ |
| WebClient (comunicación HTTP reactiva) | ✅ |
| Programación Funcional (Streams, Lambdas, Optional) | ✅ |
| WebFlux (endpoints reactivos) | ✅ |
| H2 Database | ✅ |
| JUnit 5 + Mockito | ✅ |
| 2 Microservicios comunicándose | ✅ |

## Endpoints

### Microservicio 1: Forex API (Puerto 8080)

| Método | Endpoint | Descripción |
|--------|---------|-------------|
| GET | `/forex/latest?base=EUR&symbols=USD,GBP` | Tasas actuales |
| GET | `/forex/historical?date=2024-01-15&base=EUR` | Tasas históricas |
| POST | `/forex/convert` | Convertir moneda |

### Microservicio 2: History API (Puerto 8081)

| Método | Endpoint | Descripción |
|--------|---------|-------------|
| GET | `/history` | Ver todo el historial |
| GET | `/history/user/{userId}` | Historial por usuario |
| GET | `/history/{id}` | Ver una conversión |
| POST | `/history` | Guardar conversión |

## Comunicación entre Microservicios

### Flujo de datos:

```
Usuario
   │
   ▼
┌─────────────────────────────────────────────────────────┐
│                    FOREX API (Puerto 8080)               │
│                                                         │
│  1. POST /forex/convert {from, to, amount, userId}    │
│                          │                               │
│                          ▼                               │
│  2. ForexService.getLatestRates(from, to)             │
│                          │                               │
│                          ▼                               │
│  3. FrankfurterRepository (WebClient) ──► API Externa  │
│     https://api.frankfurter.app                         │
│                          │                               │
│                          ▼                               │
│  4. HistoryClient.saveConversion() ──► HTTP POST       │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  HISTORY API (Puerto 8081)              │
│                                                         │
│  5. POST /history {datos conversion}                  │
│                          │                               │
│                          ▼                               │
│  6. ConversionHistoryRepository.save()                 │
│                          │                               │
│                          ▼                               │
│  7. H2 Database (memoria)                               │
└─────────────────────────────────────────────────────────┘
```

### Componentes clave de la conexión:

| Componente | Ubicación | Función |
|------------|-----------|---------|
| `HistoryClient` | forex-api/client/ | Llama a History API via WebClient |
| `FrankfurterRepository` | forex-api/repository/ | Consume API externa de tasas |
| `ConversionHistoryRepository` | history-api/repository/ | Persiste en H2 |

### Ejemplo de flujo real:

```bash
# 1. Usuario convierte EUR a USD
curl -X POST http://localhost:8080/forex/convert \
  -H "Content-Type: application/json" \
  -d '{"from":"EUR","to":"USD","amount":100,"userId":"juan"}'

# 2. Forex API:
#    - Obtiene tasa: 1 EUR = 1.1547 USD
#    - Calcula: 100 * 1.1547 = 115.47
#    - Guarda en History API (puerto 8081)

# 3. Ver historial
curl http://localhost:8081/history/user/juan
```

## Programación Funcional

```java
// Optional para evitar NullPointerException
Optional.ofNullable(base)
    .filter(IS_VALID_CURRENCY)
    .map(this::fetchRates)

// Streams para filtrar
rates.entrySet().stream()
    .filter(entry -> symbolFilter.test(entry.getKey()))
    .collect(Collectors.toMap())

// Lambdas y Predicates
private static final Predicate<String> IS_VALID_CURRENCY = currency -> ...

// Supplier para excepciones
private static final Supplier<ForexApiException> ERROR = () -> ...
```

## Records de Java 17

```java
// DTO inmutable
public record ConversionRecord(
    Long id,
    String fromCurrency,
    String toCurrency,
    BigDecimal amount,
    BigDecimal result,
    BigDecimal rate,
    LocalDateTime conversionDate,
    String userId
) {}
```

## Tests Unitarios

| Microservicio | Tests |
|---------------|-------|
| forex-api (ForexServiceTest) | 14 tests |
| forex-api (ForexControllerTest) | 11 tests |
| history-api (HistoryServiceTest) | 12 tests |
| history-api (HistoryControllerTest) | 9 tests |
| **Total** | **46 tests** |

Ejecutar tests:
```bash
# Forex API
cd forex-api && ./gradlew test

# History API
cd history-api && ./gradlew test

# Ambos
./gradlew test
```

## Cómo Ejecutar

```bash
# Iniciar Forex API (puerto 8080)
cd forex-api && ./gradlew bootRun

# Iniciar History API (puerto 8081)
cd history-api && ./gradlew bootRun --args='--server.port=8081'
```

## Diferencia entre Repositorios

Es importante entender que existen **dos tipos de repositorios** en esta arquitectura:

### 1. ConversionHistoryRepository (JPA Repository)
- **Acceso**: Base de datos local (H2)
- **Función**: Persiste y consulta el historial de conversiones
- **Extiende**: `JpaRepository<ConversionHistory, Long>`
- **Ubicación**: `history-api/src/main/java/com/history/api/repository/`

```java
// Acceso a BD local - métodos derivados de Spring Data
List<ConversionHistory> findByUserIdOrderByConversionDateDesc(String userId);
List<ConversionHistory> findByConversionDateBetween(LocalDateTime start, LocalDateTime end);
```

### 2. FrankfurterRepository (Cliente HTTP)
- **Acceso**: API externa (frankfurter.app - tasas de cambio)
- **Función**: Consume servicios web externos
- **NO es un JPA Repository**: Es un cliente REST con WebClient
- **Ubicación**: `forex-api/src/main/java/com/forex/api/repository/`

```java
// Consumo de API externa - NO accede a base de datos
public FrankfurterResponse getLatestRates(String base, String symbols) {
    return webClient.get()
            .uri("https://api.frankfurter.app/latest?base=" + base)
            .retrieve()
            .bodyToMono(FrankfurterResponse.class)
            .block();
}
```

> **Nota técnica**: El nombre "FrankfurterRepository" es un nombre histórico. Técnicamente debería llamarse `FrankfurterClient` o `FrankfurterService` ya que sigue el patrón de **Integration Adapter** (consumo de APIs externas), no el patrón Repository tradicional (acceso a datos locales).

## Base de Datos H2

El historial se guarda en H2 en memoria:
- URL: `jdbc:h2:mem:historydb`
- Consola: `http://localhost:8081/h2-console`
