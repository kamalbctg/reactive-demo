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
import java.util.List;


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
