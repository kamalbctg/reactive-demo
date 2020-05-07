package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


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

    @Test
    void streamOfFluxWithCreate() {
        //Programmatically create a Flux with the capability of emitting multiple elements in a synchronous or
        // asynchronous manner through the FluxSink API. This includes emitting elements from multiple threads.
        Flux<Integer> fluxTest = Flux.create((FluxSink<Integer> fluxSink) -> {
            IntStream.range(0, 5).peek(i -> System.out.println("going to emit - " + i)).forEach(fluxSink::next);
        });

        fluxTest.delayElements(Duration.ofMillis(1)).subscribe(i -> System.out.println("subscriber-1 :: " + i));
        fluxTest.delayElements(Duration.ofMillis(2)).subscribe(i -> System.out.println("subscriber-2 :: " + i));
    }

    @Test
    void streamOfFluxWithCreateAndOverFlowStrategy() {
        //Programmatically create a Flux with the capability of emitting multiple elements in a synchronous or
        // asynchronous manner through the FluxSink API. This includes emitting elements from multiple threads.
        Flux<Integer> fluxTest = Flux.create((FluxSink<Integer> fluxSink) -> {
            IntStream.range(0, 5).peek(i -> System.out.println("going to emit - " + i)).forEach(fluxSink::next);
        },FluxSink.OverflowStrategy.DROP);

        fluxTest.delayElements(Duration.ofMillis(1)).subscribe(i -> System.out.println("subscriber-1 :: " + i));
        fluxTest.delayElements(Duration.ofMillis(2)).subscribe(i -> System.out.println("subscriber-2 :: " + i));
    }

     @Test
    void streamOfFluxWithGenerate() throws InterruptedException {
         /**
          * Programmatically create a Flux by generating signals one-by-one via a consumer callback.
          * generate method is aware of downstream observers processing speed
          **/
         AtomicInteger atomicInteger = new AtomicInteger();

         Flux<Integer> integerFlux = Flux.generate((SynchronousSink<Integer> synchronousSink) -> {
             System.out.println("Flux generate"+ atomicInteger.get());
             synchronousSink.next(atomicInteger.getAndIncrement());
         });

         integerFlux.delayElements(Duration.ofMillis(50))
                 .subscribe(i -> System.out.println("First consumed ::" + i));

         Thread.sleep(Duration.ofSeconds(3).toMillis());
    }


}
