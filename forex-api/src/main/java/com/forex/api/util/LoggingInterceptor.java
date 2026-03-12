package com.forex.api.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIP = getClientIP(request);
        logger.info("Incoming request: {} {} from IP: {}", 
                   request.getMethod(), request.getRequestURI(), clientIP);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        String clientIP = getClientIP(request);
        logger.info("Completed request: {} {} with status: {} for IP: {}", 
                   request.getMethod(), request.getRequestURI(), response.getStatus(), clientIP);
        
        if (ex != null) {
            logger.error("Exception in request: {} {} from IP: {}", 
                        request.getMethod(), request.getRequestURI(), clientIP, ex);
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
