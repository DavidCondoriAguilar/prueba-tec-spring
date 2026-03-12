package com.forex.api.config;

/**
 * Constants for Frankfurter API configuration and endpoints.
 * 
 * This class centralizes all API-related constants to avoid duplication
 * and provide a single source of truth for external API configuration.
 * 
 * @author Forex API Team
 * @version 1.0
 * @since 2026-02-02
 */
public final class FrankfurterApiConstants {
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private FrankfurterApiConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // API Base Configuration
    public static final String DEFAULT_BASE_URL = "https://api.frankfurter.dev/v1";
    public static final String BASE_URL_PROPERTY = "frankfurter.base-url";
    public static final String BASE_URL_ENV_VAR = "FRANKFURTER_BASE_URL";
    
    // Timeout Configuration
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 5000;
    public static final String CONNECT_TIMEOUT_PROPERTY = "frankfurter.connect-timeout";
    public static final String READ_TIMEOUT_PROPERTY = "frankfurter.read-timeout";
    
    // API Endpoints
    public static final String LATEST_ENDPOINT = "/latest";
    public static final String HISTORICAL_ENDPOINT_FORMAT = "/%s";
    public static final String CURRENCIES_ENDPOINT = "/currencies";
    
    // Query Parameters
    public static final String BASE_PARAM = "base";
    public static final String SYMBOLS_PARAM = "symbols";
    
    // Cache Configuration
    public static final String LATEST_RATES_CACHE = "forexRates";
    public static final String HISTORICAL_RATES_CACHE = "historicalRates";
    
    // HTTP Status Codes
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_INTERNAL_ERROR = 500;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;
    
    // Error Messages
    public static final String ERROR_EXTERNAL_API_UNAVAILABLE = "No se puede conectar al servicio de tasas de cambio";
    public static final String ERROR_EXTERNAL_API_SERVER_ERROR = "Error del servidor al consultar tasas de cambio";
    public static final String ERROR_UNEXPECTED = "Error inesperado al consultar tasas de cambio";
    public static final String ERROR_NO_HISTORICAL_DATA = "No se encontraron datos para la fecha especificada";
}
