# Forex API - Spring Boot

API REST para consulta de tasas de cambio utilizando Frankfurter API.

## Arquitectura

### Estructura de Paquetes

```
src/main/java/com/forex/api/
├── config/                 # Configuración de la aplicación
│   ├── FrankfurterApiConfig.java      # Configuración API externa
│   ├── RestTemplateConfig.java        # Configuración HTTP client
│   └── CacheConfig.java               # Configuración de cache
├── controller/             # Endpoints REST
│   └── ForexController.java           # 3 endpoints principales
├── dto/                    # Data Transfer Objects
│   ├── FrankfurterResponse.java      # DTO API externa
│   ├── request/                      # DTOs entrada
│   │   ├── LatestRatesRequest.java
│   │   ├── HistoricalRatesRequest.java
│   │   └── CurrencyConversionRequest.java
│   └── response/                     # DTOs salida
│       ├── ForexRatesResponse.java
│       ├── CurrencyConversionResponse.java
│       └── ErrorResponse.java
├── entity/                 # Entidades de dominio
│   └── ForexRate.java                # Entidad principal
├── exception/              # Manejo de errores
│   ├── ForexApiException.java         # Excepción personalizada
│   └── GlobalExceptionHandler.java    # Manejo global de errores
├── mapper/                 # Transformación de datos
│   └── ForexMapper.java               # Mapeo entre DTOs y entities
├── repository/             # Acceso a datos
│   └── FrankfurterRepository.java     # Cliente API externa
├── service/                # Lógica de negocio
│   └── ForexService.java              # Servicios principales
├── util/                   # Utilidades
│   ├── LoggingInterceptor.java        # Interceptor de logging
│   └── WebConfig.java                  # Configuración web
└── validation/             # Validaciones
    └── ForexValidator.java            # Validador de entrada
```

### Stack Tecnológico

- **Framework**: Spring Boot 4.0.2
- **Java**: 17
- **Build**: Gradle 9.3.0
- **HTTP Client**: RestTemplate
- **Cache**: Spring Cache (ConcurrentMapCache)
- **Validation**: Jakarta Validation
- **JSON**: Jackson
- **Logging**: SLF4J + Logback
- **Lombok**: Reducción de boilerplate

### Patrones Implementados

- **DTO Pattern**: Separación entrada/salida
- **Repository Pattern**: Abstracción acceso a API externa
- **Service Layer**: Lógica de negocio centralizada
- **Controller Layer**: Exposición REST
- **Builder Pattern**: Construcción de objetos
- **Dependency Injection**: Inyección via constructor
- **Global Exception Handling**: Manejo centralizado de errores

## Endpoints

### GET /forex/latest
```bash
GET /forex/latest?base=USD&symbols=EUR,GBP,JPY
```
- **Propósito**: Tasas de cambio actuales
- **Parámetros**: `base` (opcional), `symbols` (opcional)
- **Response**: `ForexRatesResponse`

### GET /forex/historical
```bash
GET /forex/historical?date=2024-12-20&base=USD&symbols=EUR,GBP
```
- **Propósito**: Tasas de cambio históricas
- **Parámetros**: `date` (requerido), `base` (opcional), `symbols` (opcional)
- **Response**: `ForexRatesResponse`

### POST /forex/convert
```bash
POST /forex/convert
Content-Type: application/json

{
  "from": "USD",
  "to": "EUR",
  "amount": 1000
}
```
- **Propósito**: Conversión de divisas
- **Body**: `CurrencyConversionRequest`
- **Response**: `CurrencyConversionResponse`

## Configuración

### Variables de Entorno

```bash
PORT=8080                                    # Puerto servidor
FRANKFURTER_BASE_URL=https://api.frankfurter.dev/v1  # URL API externa
```

### Cache

- **Tipo**: In-memory (ConcurrentMapCache)
- **Caches**: `forexRates`, `historicalRates`
- **TTL**: Por defecto de Spring Cache

## Validaciones

### Divisas
- **Formato**: Códigos ISO 4217 (3 letras mayúsculas)
- **Validación**: 160+ divisas soportadas
- **Ejemplo**: USD, EUR, GBP, JPY

### Fechas
- **Formato**: YYYY-MM-DD
- **Rango**: 1999-01-04 a fecha actual
- **Validación**: Formato y rango temporal

### Montos
- **Tipo**: BigDecimal
- **Restricción**: Mayor a 0.01
- **Precisión**: 2 decimales

## Manejo de Errores

### Estructura de Error
```json
{
  "timestamp": "2026-02-02T21:33:31.303768866",
  "error": "Mensaje descriptivo en español",
  "statusCode": 400
}
```

### Códigos de Error
- **400**: Bad Request - Validación fallida
- **404**: Not Found - Recurso no encontrado
- **500**: Internal Server Error - Error interno
- **503**: Service Unavailable - API externa caída

## Instalación y Ejecución

### Prerrequisitos
- Java 17+
- Gradle 7.0+

### Build
```bash
./gradlew build
```

### Ejecución
```bash
./gradlew bootRun
```

### Testing
```bash
# Latest rates
curl "http://localhost:8080/forex/latest?base=USD&symbols=EUR,GBP"

# Historical rates
curl "http://localhost:8080/forex/historical?date=2024-12-20&base=USD&symbols=EUR"

# Currency conversion
curl -X POST http://localhost:8080/forex/convert \
  -H "Content-Type: application/json" \
  -d '{"from":"USD","to":"EUR","amount":1000}'
```

## API Externa

**Frankfurter API**
- **URL**: https://api.frankfurter.dev/v1
- **Autenticación**: No requiere API key
- **Límites**: Sin límites de uso
- **Datos históricos**: Desde 1999-01-04
- **Actualización**: Diaria

## Logging

### Niveles Configurados
- **INFO**: Request/response del controller
- **DEBUG**: Flujo interno de servicios
- **WARN**: Validaciones fallidas
- **ERROR**: Errores de API externa

### Pattern
```
%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

## Performance

### Cache Strategy
- **Latest rates**: Cache por `(base, symbols)`
- **Historical rates**: Cache por `(date, base, symbols)`
- **TTL**: Configurable via Spring Cache

### Timeout Configuration
- **Connect**: 5000ms
- **Read**: 5000ms
- **Retry**: No implementado (fallback a error)

## Arquitectura de Datos

### Flujo de Request
```
HTTP Request → Controller → Request DTO → Service → Validator → Repository → External API
```

### Flujo de Response
```
External API → Repository → FrankfurterResponse → Mapper → Entity → Response DTO → Controller → HTTP Response
```

### Transformaciones
1. **Request Params → Request DTO**: Controller layer
2. **Request DTO → Validation**: Service layer
3. **FrankfurterResponse → Entity**: Mapper layer
4. **Entity → Response DTO**: Mapper layer

## Consideraciones de Producción

### Escalabilidad
- **Stateless**: Todos los componentes son stateless
- **Cache**: In-memory (considerar Redis para cluster)
- **Rate Limiting**: No implementado (recomendado para producción)

### Seguridad
- **CORS**: No configurado
- **Authentication**: No implementado
- **HTTPS**: Depende de reverse proxy

### Monitoring
- **Health Check**: Spring Boot Actuator disponible
- **Metrics**: No configuradas (recomendado Micrometer)
- **Distributed Tracing**: No implementado
