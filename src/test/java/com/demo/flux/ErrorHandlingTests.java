package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


class ErrorHandlingTests {

    //In streams, errors are terminal events. This means that from the point we encounter an error,
    // our stream is not processed by the designated operator.
    @Test
    void streamOfFluxWithError() {
        Flux<Integer> fluxTest = Flux.just(1, 2, 0, 5)
                .map(d -> 100 / d)
                .log();
        StepVerifier.create(fluxTest)
                .expectNext(100)
                .expectNext(50)
                .expectError(ArithmeticException.class)
                .verify();
    }


    @Test
    void streamOfFluxWithError2() {
        Flux<Integer> fluxTest = Flux.just(1, 2, 0, 5)
                .map(d -> 100 / d)
                .onErrorReturn(Integer.MAX_VALUE)
                .log();
        StepVerifier.create(fluxTest)
                .expectNext(100)
                .expectNext(50)
                .expectNext(Integer.MAX_VALUE)
                .verifyComplete();
    }


    @Test
    void streamOfFluxWithError3() {
        Flux<Integer> fluxTest = Flux.just(1, 2, 0, 5)
                .map(d -> 100 / d)
                .onErrorResume(ex -> Flux.just(6, 7)) //provide a fallback publisher
                .log();
        StepVerifier.create(fluxTest)
                .expectNext(100)
                .expectNext(50)
                .expectNext(6)
                .expectNext(7)
                .verifyComplete();
    }


    @Test
    void streamOfFluxWithError4() {
        Flux<Integer> fluxTest = Flux.just(1, 2, 0, 5)
                .map(d -> 100 / d)
                .onErrorMap(ex -> new IllegalArgumentException("opps")) //provide a fallback publisher
                .log();
        StepVerifier.create(fluxTest)
                .expectNext(100)
                .expectNext(50)
                .expectError(IllegalArgumentException.class)
                .verify();
    }


    @Test
    void streamOfFluxWithError41() {
        Flux<Integer> fluxTest = Flux.just(1, 2, 0, 5)
                .map(d -> 100 / d)
                .doOnError(System.out::println) //It’ll catch, perform side-effect operation and rethrow the exception
                .log();
        StepVerifier.create(fluxTest)
                .expectNext(100)
                .expectNext(50)
                .expectError(ArithmeticException.class)
                .verify();
    }

    @Test
    void streamOfFluxWithError5() {
        Flux<String> fluxTest = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Opps")))
                .doFinally(it -> {
                    if (SignalType.ON_COMPLETE == it) {
                        System.out.println("Complete");
                    }
                    if (SignalType.ON_ERROR == it) {
                        System.out.println("Error");
                    }
                })
                .log();
        StepVerifier.create(fluxTest)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .expectError(RuntimeException.class)
                .verify();
    }


    @Test
    void streamOfFluxWithError6() {
        Flux<String> fluxTest = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Opps")))
                .onErrorContinue((throwable, o) -> {
                    System.out.println(o);
                })
                .concatWith(Flux.just("D"))
                .log();
        StepVerifier.create(fluxTest)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .expectError(RuntimeException.class)
                .verify();
    }


    @Test
    public void testCheckedExceptions() {
        /**
         Cannot throw checked exceptions from the producer and the subscriber. Reactor provides the Exceptions
         utility class for handling such scenarios. The Exceptions class provides a propagate method,
         which can wrap any checked exception into an unchecked exception, as follows:
         */
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long, Long>of(0L, 1L), (state, sink) -> {
            try {
                raiseCheckedException();
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        fibonacciGenerator
                .subscribe(System.out::println,
                        e -> Exceptions.unwrap(e).printStackTrace());
    }


    @Test
    public void testTimeout() throws Exception {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long,
                Long>of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0)
                throw new RuntimeException("Value out of bounds");
            else
                sink.next(state.getT1());

            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        fibonacciGenerator
                .delayElements(Duration.ofSeconds(1))
                .timeout(Duration.ofMillis(500))
                .subscribe(System.out::println, e -> {
                    System.out.println(e);
                    countDownLatch.countDown();
                });
        countDownLatch.await();
    }


    @Test
    public void testTimeoutWithFallback() throws Exception {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long,
                Long>of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0)
                throw new RuntimeException("Value out of bounds");
            else
                sink.next(state.getT1());

            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        CountDownLatch countDownLatch = new CountDownLatch(1);
        fibonacciGenerator
                .delayElements(Duration.ofSeconds(1))
                .timeout(Duration.ofMillis(500), Flux.just(1l))
                .subscribe(System.out::println, e -> {
                    System.out.println(e);
                    countDownLatch.countDown();
                });
        countDownLatch.await();
    }

    @Test
    public void testRetry() throws Exception {

        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long,
                Long>of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0)
                throw new RuntimeException("Value out of bounds");
            else
                sink.next(state.getT1());

            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });

        CountDownLatch countDownLatch = new CountDownLatch(1);
        fibonacciGenerator
                .retry(1)
                .log()
                .subscribe(System.out::println, e -> {
                    System.out.println("received :" + e);
                    countDownLatch.countDown();
                }, countDownLatch::countDown);
        countDownLatch.await();
    }

    @Test
    public void testReactorComposite() throws Exception {
        Flux.just(1, 2, 3, 4, 5, 6, 7, 8)
                .publishOn(Schedulers.parallel())
                .filter(x -> {
                    System.out.println("Executing Filter");
                    return x % 2 == 0;
                })
                .log()
                .doOnNext(x -> System.out.println("Next value is  " + x))
                .doFinally(x -> System.out.println("Closing "))
                .subscribeOn(Schedulers.single())
                .subscribe(x -> System.out.println("Sub received : " + x));
        Thread.sleep(500);
    }

    void raiseCheckedException() throws IOException {
        throw new IOException("Raising checked Exception");
    }

    private List<String> toUpperCase(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return List.of(s.toUpperCase(), Thread.currentThread().getName());
    }

    private String toLowerCase(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return s.toLowerCase();
    }

    private List<User> getUsers() {
        String[] names = new String[]{"1.kamal", "2.jamal", "3.rahim", "4.korim", "5.nayeem"};
        final List<User> users = new ArrayList<>();
        for (String name : names) {
            User user = new User(name);
            users.add(user);
        }
        return users;
    }

    public class User {
        private String name;

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
