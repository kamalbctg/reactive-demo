package com.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

@RestController
public class FluxExampleController {
    @GetMapping("/flux")
    public Flux<Integer> getNumbers() {
        return Flux.just(1,2,3,4,5)
                .delayElements(Duration.ofSeconds(1))
                .log();
    }

    @GetMapping(value = "/fluxStream", produces = {APPLICATION_STREAM_JSON_VALUE})
    public Flux<Integer>  getNumberStream()  {
        return Flux.just(1,2,3,4,5)
                .delayElements(Duration.ofSeconds(1))
                .log();
    }
}
