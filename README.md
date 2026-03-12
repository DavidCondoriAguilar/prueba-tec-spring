# Challenge Técnico - Microservicios Forex

## Arquitectura General

```
Usuario
   │
   ▼
┌─────────────────────────────────────────────────────────────┐
│                  FOREX API (Puerto 8080)                     │
│  Controller → Service → FrankfurterRepository (API externa) │
│              ↘️→ HistoryClient → HTTP POST                   │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  HISTORY API (Puerto 8081)                   │
│  Controller → Service → Repository → H2 Database           │
└─────────────────────────────────────────────────────────────┘
```

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

## Entidades y Lógica de Negocio

### ¿Qué pasa cuando conviertes moneda?

```
POST /forex/convert
{ "from": "EUR", "to": "USD", "amount": 100, "userId": "juan" }

         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│  1. VALIDAR                                               │
│     - from/to = 3 letras mayúsculas                       │
│     - amount > 0                                           │
│     - from != to                                          │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│  2. OBTENER TASA (Frankfurter API)                        │
│     GET /latest?base=EUR&symbols=USD                      │
│     → {"base": "EUR", "rates": {"USD": 1.1547}}         │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│  3. CALCULAR                                              │
│     result = amount × rate                                │
│     100 × 1.1547 = 115.47                                 │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│  4. GUARDAR EN HISTORIA (History API)                     │
│     POST /history {fromCurrency, toCurrency, amount...}   │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│  5. RESPONDER                                             │
│     {from, to, amount, rate, result, date}                │
└─────────────────────────────────────────────────────────────┘
```

### Entidades y DTOs

| Clase | Tipo | Uso |
|-------|------|-----|
| `ConversionHistory` | Entity (JPA) | Se guarda en H2 |
| `ConversionRecord` | Record (DTO) | Comunicación entre servicios |
| `CurrencyConversionRequest` | DTO | Lo que mandas al API |
| `CurrencyConversionResponse` | DTO | Lo que recibes del API |

### ¿Por qué separate Entidad y DTO?

```
┌─────────────────────────────────────────────────────────────┐
│  ConversionHistory (Entity)                                 │
│  - Mutable (setters)                                       │
│  - Anotaciones @Entity, @Column                            │
│  - Solo vive en history-api                                │
└─────────────────────────────────────────────────────────────┘
                           │ mapper
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  ConversionRecord (Record)                                  │
│  - Inmutable                                               │
│  - Sin setters, menos código                               │
│  - Viaja entre forex-api y history-api                    │
└─────────────────────────────────────────────────────────────┘
```

### Por qué BigDecimal para dinero?

```java
// ❌ Double tiene errores de precisión
double x = 0.1 + 0.2;  // 0.30000000000000004

// ✅ BigDecimal es exacto
BigDecimal x = new BigDecimal("0.1").add(new BigDecimal("0.2"));  // 0.3
```

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

## Conceptos Clave

### Programación Funcional (Optional, Predicate, Lambda)

```java
// Predicate: validar moneda (reusable, inmutable)
private static final Predicate<String> IS_VALID_CURRENCY = currency ->
    Optional.ofNullable(currency)
        .map(String::trim)
        .filter(c -> !c.isEmpty())
        .isPresent();

// Uso: código limpio, sin if/else
return Optional.ofNullable(base)
    .filter(IS_VALID_CURRENCY)
    .map(this::executeLatestRatesFetch)
    .orElseThrow(VALIDATION_ERROR);
```

### Records (Java 17)

```java
// DTO inmutable - sin getters/setters/equals/hashCode
public record ConversionRecord(
    Long id,
    String fromCurrency,
    BigDecimal amount,
    BigDecimal result
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

## Diferencia entre Componentes

| Componente | Tipo | Qué hace |
|------------|------|----------|
| `FrankfurterRepository` | Cliente HTTP (WebClient) | Consume API externa frankfurter.app |
| `HistoryClient` | Cliente HTTP (WebClient) | Llama a History API (otro microservicio) |
| `ConversionHistoryRepository` | JPA Repository | Accede a H2 (base de datos) |

> **Nota**: "FrankfurterRepository" debería llamarse "FrankfurterClient" - no es JPA, es un cliente HTTP.

## Tests: 46 total

| Microservicio | Tests |
|---------------|-------|
| forex-api (Service + Controller) | 25 |
| history-api (Service + Controller) | 21 |

Ejecutar: `./gradlew test`
