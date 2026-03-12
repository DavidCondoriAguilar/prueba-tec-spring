# PROMPT COMPLETO - Reconstrucción Forex API Spring Boot

## INSTRUCCIONES
Crea una API REST de Forex con Spring Boot que implemente exactamente la siguiente arquitectura enterprise con todas las optimizaciones senior:

## REQUERIMIENTOS TÉCNICOS

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

### Dependencias build.gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### Configuración application.properties
```properties
spring.application.name=forex-api
server.port=${PORT:8080}
frankfurter.base-url=${FRANKFURTER_BASE_URL:https://api.frankfurter.dev/v1}
frankfurter.connect-timeout=5000
frankfurter.read-timeout=5000
spring.cache.type=simple
spring.cache.cache-names=${CACHE_NAMES:forexRates,historicalRates}
logging.level.com.forex.api=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

## ARQUITECTURA DE PAQUETES

```
src/main/java/com/forex/api/
├── config/
│   ├── FrankfurterApiConfig.java      # @ConfigurationProperties con @Data @Builder
│   ├── RestTemplateConfig.java        # RestTemplate con timeouts
│   ├── CacheConfig.java               # @EnableCaching con ConcurrentMapCache
│   └── FrankfurterApiConstants.java  # Clase final con constantes centralizadas
├── controller/
│   └── ForexController.java           # 3 endpoints, sin logging excesivo
├── dto/
│   ├── FrankfurterResponse.java      # DTO API externa con @Data @Builder
│   ├── request/
│   │   ├── LatestRatesRequest.java   # @Builder @Valid @NotBlank
│   │   ├── HistoricalRatesRequest.java # @Builder @Valid @NotBlank
│   │   └── CurrencyConversionRequest.java # @Builder @Valid @Positive
│   └── response/
│       ├── ForexRatesResponse.java  # @Data @Builder
│       ├── CurrencyConversionResponse.java # @Data @Builder
│       └── ErrorResponse.java       # @Data @Builder
├── entity/
│   └── ForexRate.java               # @Data @Builder
├── exception/
│   ├── ForexApiException.java        # RuntimeException con statusCode
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
├── mapper/
│   └── ForexMapper.java              # @Slf4j con builders
├── repository/
│   └── FrankfurterRepository.java    # @Repository @Cacheable
├── service/
│   └── ForexService.java             # @Service @RequiredArgsConstructor
├── util/
│   ├── LoggingInterceptor.java       # HandlerInterceptor
│   └── WebConfig.java                # @Configuration implements WebMvcConfigurer
└── validation/
    └── ForexValidator.java           # @Slf4j con validaciones ISO 4217
```

## IMPLEMENTACIÓN DETALLADA

### 1. Constants Class
```java
public final class FrankfurterApiConstants {
    private FrankfurterApiConstants() { throw new UnsupportedOperationException(); }
    
    public static final String DEFAULT_BASE_URL = "https://api.frankfurter.dev/v1";
    public static final String LATEST_ENDPOINT = "/latest";
    public static final String HISTORICAL_ENDPOINT_FORMAT = "/%s";
    public static final String CURRENCIES_ENDPOINT = "/currencies";
    public static final String BASE_PARAM = "base";
    public static final String SYMBOLS_PARAM = "symbols";
    public static final String LATEST_RATES_CACHE = "forexRates";
    public static final String HISTORICAL_RATES_CACHE = "historicalRates";
    public static final int HTTP_OK = 200;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;
    public static final String ERROR_EXTERNAL_API_UNAVAILABLE = "No se puede conectar al servicio de tasas de cambio";
    public static final String ERROR_EXTERNAL_API_SERVER_ERROR = "Error del servidor al consultar tasas de cambio";
    public static final String ERROR_UNEXPECTED = "Error inesperado al consultar tasas de cambio";
}
```

### 2. Controller Layer
```java
@RestController
@RequestMapping("/forex")
@RequiredArgsConstructor
public class ForexController {
    private final ForexService forexService;
    
    @GetMapping("/latest")
    public ResponseEntity<ForexRatesResponse> getLatestRates(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam(required = false) String symbols) {
        ForexRatesResponse response = forexService.getLatestRates(base, symbols);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/historical")
    public ResponseEntity<ForexRatesResponse> getHistoricalRates(
            @RequestParam String date,
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam(required = false) String symbols) {
        ForexRatesResponse response = forexService.getHistoricalRates(date, base, symbols);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convertCurrency(@Valid @RequestBody CurrencyConversionRequest request) {
        CurrencyConversionResponse response = forexService.convertCurrency(request);
        return ResponseEntity.ok(response);
    }
}
```

### 3. Service Layer
```java
@Service
@RequiredArgsConstructor
public class ForexService {
    private final FrankfurterRepository repository;
    private final ForexMapper mapper;
    private final ForexValidator validator;
    
    public ForexRatesResponse getLatestRates(String base, String symbols) {
        LatestRatesRequest request = LatestRatesRequest.builder()
                .base(base)
                .symbols(symbols)
                .build();
        
        validator.validateCurrencyCode(request.getBase());
        if (request.getSymbols() != null && !request.getSymbols().trim().isEmpty()) {
            validator.validateCurrencySymbols(request.getSymbols());
        }
        
        FrankfurterResponse response = repository.getLatestRates(
            request.getBase() != null ? request.getBase() : "EUR", 
            request.getSymbols()
        );
        
        ForexRate forexRate = mapper.toForexRate(response);
        return mapper.toForexRatesResponse(forexRate);
    }
    
    // Métodos similares para getHistoricalRates y convertCurrency
}
```

### 4. Repository Layer
```java
@Repository
@RequiredArgsConstructor
public class FrankfurterRepository {
    private final RestTemplate restTemplate;
    private final FrankfurterApiConfig config;
    
    @Cacheable(value = FrankfurterApiConstants.LATEST_RATES_CACHE, key = "{#base, #symbols}")
    public FrankfurterResponse getLatestRates(String base, String symbols) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                config.getBaseUrl() + FrankfurterApiConstants.LATEST_ENDPOINT)
                .queryParam(FrankfurterApiConstants.BASE_PARAM, base);
            
            if (symbols != null && !symbols.trim().isEmpty()) {
                builder.queryParam(FrankfurterApiConstants.SYMBOLS_PARAM, symbols);
            }
            
            return restTemplate.getForObject(builder.toUriString(), FrankfurterResponse.class);
            
        } catch (HttpClientErrorException e) {
            throw new ForexApiException("Error al consultar tasas de cambio actuales: " + e.getMessage(), e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            throw new ForexApiException(FrankfurterApiConstants.ERROR_EXTERNAL_API_SERVER_ERROR, FrankfurterApiConstants.HTTP_SERVICE_UNAVAILABLE);
        } catch (ResourceAccessException e) {
            throw new ForexApiException(FrankfurterApiConstants.ERROR_EXTERNAL_API_UNAVAILABLE, FrankfurterApiConstants.HTTP_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            throw new ForexApiException(FrankfurterApiConstants.ERROR_UNEXPECTED, FrankfurterApiConstants.HTTP_INTERNAL_ERROR);
        }
    }
    
    // Métodos similares para getHistoricalRates y getAvailableCurrencies
}
```

### 5. DTOs con Lombok
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestRatesRequest {
    @NotBlank(message = "La divisa base es requerida")
    @Pattern(regexp = "^[A-Z]{3}$", message = "La divisa debe tener 3 letras mayúsculas")
    private String base;
    
    private String symbols;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForexRatesResponse {
    private String base;
    private String date;
    private Map<String, Double> rates;
}
```

### 6. Validation Layer
```java
@Component
@Slf4j
public class ForexValidator {
    private static final Set<String> VALID_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "SEK", "NOK", "DKK"
        // ... 160+ currencies
    );
    
    public void validateCurrencyCode(String currency) {
        if (currency != null && !VALID_CURRENCIES.contains(currency)) {
            throw new ForexApiException("Divisa no válida: " + currency, 400);
        }
    }
    
    // Otros métodos de validación
}
```

## ENDPOINTS ESPERADOS

### GET /forex/latest
- Query params: `base` (default EUR), `symbols` (optional)
- Response: `ForexRatesResponse`

### GET /forex/historical  
- Query params: `date` (required), `base` (default EUR), `symbols` (optional)
- Response: `ForexRatesResponse`

### POST /forex/convert
- Body: `CurrencyConversionRequest`
- Response: `CurrencyConversionResponse`

## CARACTERÍSTICAS OBLIGATORIAS

1. **Constants Centralizadas**: FrankfurterApiConstants con todas las constantes
2. **Lombok**: @Data, @Builder, @RequiredArgsConstructor en todas las clases
3. **Cache Declarativo**: @Cacheable con keys específicos
4. **Type Safety**: defaultValue en @RequestParam, @Valid en DTOs
5. **Error Handling**: GlobalExceptionHandler con ForexApiException
6. **Clean Code**: Sin logging excesivo, código auto-documentado
7. **Separation of Concerns**: Controller no construye DTOs, Service sí
8. **Configuration Properties**: FrankfurterApiConfig con @ConfigurationProperties

## VALIDACIONES REQUERIDAS

- **Divisas**: Códigos ISO 4217 (3 letras mayúsculas)
- **Fechas**: Formato YYYY-MM-DD, rango 1999-01-04 a actualidad
- **Montos**: BigDecimal > 0.01, 2 decimales

## TESTING POSTMAN

Incluir colección Postman con:
- Latest rates (con y sin parámetros)
- Historical rates (con fecha específica)
- Currency conversion
- Tests de error (fecha inválida, divisa inválida, monto negativo)

## README COMPLETO

Documentación técnica incluyendo:
- Arquitectura de paquetes
- Stack tecnológico
- Endpoints con ejemplos
- Configuración de variables de entorno
- Estrategia de cache
- Manejo de errores
- Consideraciones de producción

## CRITERIOS DE ÉXITO

1. ✅ Build exitoso sin warnings
2. ✅ Todos los endpoints funcionales
3. ✅ Cache trabajando correctamente
4. ✅ Validaciones implementadas
5. ✅ Error handling robusto
6. ✅ Código limpio y mantenible
7. ✅ Documentación completa

Implementa esta arquitectura enterprise con todas las optimizaciones senior mencionadas.
