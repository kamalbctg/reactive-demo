package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


class TransformAndCombineTests {

    @Test
    void filterTest() {
        /**
         * Evaluate each source value against the given Predicate. If the predicate test
         * succeeds, the value is emitted. If the predicate test fails, the value is ignored
         **/
        Flux<String> fluxTest = Flux.just("Dhaka", "Chittagong", "Sylhet", "Feni")
                .filter(name -> name.length() > 4)
                .log();
        StepVerifier.create(fluxTest)
                .expectNext("Dhaka", "Chittagong", "Sylhet")
                .verifyComplete();
    }

    @Test
    void mapTest() {
        /**
         * Transform the items emitted by this Flux by applying a synchronous function to each item
         */
        Flux.fromIterable(getUsers())
                .map(u -> toUpperCase(u.getName()))
                .doOnNext(System.out::println)
                .blockLast();
    }

    @Test
    void flatMapTest() {
        /**
         Transform the elements emitted by this Flux asynchronously into Publishers, then flatten these
         inner publishers into a single Flux through merging, which allow them to interleave.
         */
        Flux.fromIterable(getUsers())
                //.map(u -> toLowerCase(u.getName()))
                .flatMap(u -> toLowerCase(u.getName()))
                .log()
                .doOnNext(d -> System.out.println(d))
                .blockLast();

    }

    @Test
    void flatMapParallelTest() {
        /**
         parallel execution
         */
        Flux.fromIterable(getUsers())
                .window(2)
                .flatMap(users -> users.flatMap(u -> toLowerCase(u.getName())).subscribeOn(Schedulers.parallel()).log())
                .log()
                .doOnNext(d -> System.out.println(d))
                .blockLast();

    }

    @Test
    void concatMapTest() {
        /**
         similar to a flatMap except that it keeps the order of the observables
         */
        Flux.fromIterable(getUsers())
                .window(2)
                .concatMap(users -> users.flatMap(u -> toLowerCase(u.getName())).subscribeOn(Schedulers.parallel()).log())
                .log()
                .doOnNext(d -> System.out.println(d))
                .blockLast();

    }


    //A SwitchMap flattens the source observable but only returns the last emitted single observable.
    @Test
    void switchMapTest() {
        /**
         * SwitchMap flattens the source observable but only returns the last emitted single observable.
         * */
        Flux.fromIterable(getUsers())
                .window(2)
                .switchMap(users -> users.flatMap(u -> toLowerCase(u.getName())).subscribeOn(Schedulers.parallel()).log())
                .log()
                .doOnNext(d -> System.out.println(d))
                .blockLast();
    }

    //combine stream without order.
    @Test
    void mergeTest() {
        Flux<String> flux1 = Flux.just("a", "b", "c");
        Flux<String> flux2 = Flux.just("e", "f", "g");

        Flux<String> mergedFlux = Flux.merge(flux1, flux2);

        StepVerifier.create(mergedFlux.log())
                .expectNext("a", "b", "c", "e", "f", "g")
                .verifyComplete();
    }

    @Test
    void mergeWithDelayTest() {
        Flux<String> flux1 = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("e", "f", "g").delayElements(Duration.ofSeconds(1));

        Flux<String> mergedFlux = Flux.merge(flux1, flux2);

        StepVerifier.create(mergedFlux.log())
                //.expectNext("a", "b", "c","e", "f", "g")
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void concatTest() {
        Flux<String> flux1 = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("e", "f", "g").delayElements(Duration.ofSeconds(1));

        Flux<String> mergedFlux = Flux.concat(flux1, flux2);

        StepVerifier.create(mergedFlux.log())
                .expectNext("a", "b", "c", "e", "f", "g")
                .verifyComplete();
    }


    @Test
    void zipTest() {
        Flux<String> flux1 = Flux.just("a", "b", "c");
        Flux<String> flux2 = Flux.just("g", "f", "e");

        Flux<Tuple2<String, String>> mergedFlux = Flux.zip(flux1, flux2);
        Flux<String> combined = mergedFlux.map(t -> t.getT1() + t.getT2());
        StepVerifier.create(combined.log())
                .expectNext("ag", "bf", "ce")
                .verifyComplete();
    }


    @Test
    void zipWithTest() {
        Flux<String> flux1 = Flux.just("a", "b", "c");
        Flux<String> flux2 = Flux.just("g", "f", "e");

        Flux<Tuple2<String, String>> mergedFlux = flux1.zipWith(flux2);
        Flux<String> combined = mergedFlux.map(t -> t.getT1() + t.getT2());
        StepVerifier.create(combined.log())
                .expectNext("ag", "bf", "ce")
                .verifyComplete();
    }

    private List<String> toUpperCase(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return List.of(s.toUpperCase(), Thread.currentThread().getName());
    }

    private Mono<String> toLowerCase(String s) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Mono.just(s.toLowerCase());
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
