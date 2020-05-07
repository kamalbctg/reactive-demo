package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class CreatingSequenceTests {

    @Test
    void emptyFlux() {
        //Create a publisher that completes without emitting any item.
        Flux emptyFlux = Flux.empty().log();

        StepVerifier.create(emptyFlux).verifyComplete();
    }

    @Test
    void streamOfFluxWithJust() {
        //Create N publishers that emits the provided elements and then completes.
        Flux<String> fluxTest = Flux.just("A", "B", "C").log();

        StepVerifier.create(fluxTest)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .verifyComplete();
    }

    @Test
    void streamOfFluxWithIterable() {
        Flux<String> fluxTest = Flux.fromIterable(Arrays.asList("A", "B", "C")).log();

        StepVerifier.create(fluxTest)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .verifyComplete();
    }

    @Test
    void streamOfFluxWithRange() {
        //Emits a sequence of count incrementing integers, starting from start.

        Flux<Integer> fluxTest = Flux.range(1, 3).log();

        StepVerifier.create(fluxTest)
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    void streamOfFluxWithInterval() {
        //Emits long values starting with 0 and incrementing at specified time intervals on the global timer.
        Flux<Long> fluxTest = Flux.interval(Duration.ofSeconds(1)).take(2).log();

        StepVerifier
                .withVirtualTime(() -> fluxTest)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNext(0L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void streamOfFluxWithFromArray() {
        //Create a Flux that emits the items contained in the provided array..
        Flux<String> fluxTest = Flux.fromArray(new String[] {"A", "B", "C"}).log();

        StepVerifier.create(fluxTest)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .verifyComplete();
    }

    @Test
    void streamOfFluxWithFromStream() {
        //Create a Flux that emits the items contained in the provided Stream.
        Flux<String> fluxTest = Flux.fromStream(Arrays.asList("A", "B", "C").stream()).log();

        StepVerifier.create(fluxTest)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .verifyComplete();
    }
}
