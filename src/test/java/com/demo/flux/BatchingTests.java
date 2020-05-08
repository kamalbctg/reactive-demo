package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


class BatchingTests {

    @Test
    void bufferTest() {
        Flux.range(1, 15)
                .buffer(4)
                .subscribe(d -> System.out.println("onNext: {}" + d));
    }

    @Test
    void limitRequestTest() {
        Flux<Flux<Integer>> windowedFlux = Flux.range(101, 20)
                .windowUntil(BatchingTests::isPrime, true);

        windowedFlux.subscribe(window -> window
                .collectList()
                .subscribe(d -> System.out.println("window: {}"+ d)));
    }

    @Test
    void windowTest() {
        Flux.range(1, 15)
                .window(4)
                .subscribe(w -> w.buffer().subscribe( d -> System.out.println("onNext: {}" + d)));
    }

    @Test
    void groupByTest() {
        Flux.range(1, 7)
                .groupBy(e -> e % 2 == 0 ? "Even" : "Odd")
                .subscribe(g -> g.buffer().subscribe( d -> System.out.println("onNext: {}" + d)));
    }



    public static boolean isPrime(int number) {
        if (number == 2 || number == 3) {
            return true;
        }
        if (number % 2 == 0) {
            return false;
        }
        int sqrt = (int) Math.sqrt(number) + 1;
        for (int i = 3; i < sqrt; i += 2) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }


}
