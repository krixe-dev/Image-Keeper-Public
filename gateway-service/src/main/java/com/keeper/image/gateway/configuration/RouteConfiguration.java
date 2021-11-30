package com.keeper.image.gateway.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routes configuration class
 */
@Configuration
public class RouteConfiguration {

    /**
     * Build route rules handled by gateway-service
     */
    @Bean
    RouteLocator gateway(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                        .route("manager-service-route", routeSpecification -> routeSpecification.path("/images", "/images/**")
                                .uri("lb://manager-service")).build();
    }

}
