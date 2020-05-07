package com.demo.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class ProductRouter {
    @Bean
    public RouterFunction<ServerResponse> route(ProductRouteHandler productRouteHandler) {
        return RouterFunctions
                .route(GET("/v2/products").and(RequestPredicates.accept(APPLICATION_STREAM_JSON)), productRouteHandler::findAllProducts)
                .andRoute(GET("/v2/products/{id}").and(RequestPredicates.accept(APPLICATION_STREAM_JSON)), productRouteHandler::findProductById);
    }
}
