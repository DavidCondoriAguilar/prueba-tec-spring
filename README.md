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

1. Usuario llama a `/forex/convert` en Forex API
2. Forex API obtiene la tasa de cambio de Frankfurter API
3. Forex API guarda la conversión en History API usando WebClient
4. History API guarda en H2 Database

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

- **ForexServiceTest**: 14 tests con Mockito + AssertJ
- **ForexControllerTest**: 11 tests con Mockito + AssertJ

Ejecutar tests:
```bash
cd forex-api && ./gradlew test
```

## Cómo Ejecutar

```bash
# Iniciar Forex API (puerto 8080)
cd forex-api && ./gradlew bootRun

# Iniciar History API (puerto 8081)
cd history-api && ./gradlew bootRun --args='--server.port=8081'
```

## Base de Datos H2

El historial se guarda en H2 en memoria:
- URL: `jdbc:h2:mem:historydb`
- Consola: `http://localhost:8081/h2-console`
