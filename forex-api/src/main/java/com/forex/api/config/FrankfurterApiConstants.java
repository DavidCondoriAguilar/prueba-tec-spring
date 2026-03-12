package com.forex.api.config;

/**
 * Constantes - Centraliza configuración de API externa.
 */
public final class FrankfurterApiConstants {
    
    private FrankfurterApiConstants() {}
    
    // API
    public static final String DEFAULT_BASE_URL = "https://api.frankfurter.dev/v1";
    
    // Timeouts
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 5000;
    
    // Endpoints
    public static final String LATEST_ENDPOINT = "/latest";
    public static final String HISTORICAL_ENDPOINT_FORMAT = "/%s";
    public static final String CURRENCIES_ENDPOINT = "/currencies";
    
    // Parámetros
    public static final String BASE_PARAM = "base";
    public static final String SYMBOLS_PARAM = "symbols";
    
    // Cache
    public static final String LATEST_RATES_CACHE = "forexRates";
    public static final String HISTORICAL_RATES_CACHE = "historicalRates";
    
    // HTTP
    public static final int HTTP_OK = 200;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;
    
    // Errores
    public static final String ERROR_EXTERNAL_API_UNAVAILABLE = "No se puede conectar al servicio de tasas de cambio";
    public static final String ERROR_EXTERNAL_API_SERVER_ERROR = "Error del servidor al consultar tasas de cambio";
    public static final String ERROR_NO_HISTORICAL_DATA = "No se encontraron datos para la fecha especificada";
}
