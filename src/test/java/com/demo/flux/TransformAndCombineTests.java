package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


class TransformAndCombineTests {

    @Test
    void publisherWithFilter() {
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
    void streamOfFluxWithMap() {
        /**
         * Transform the items emitted by this Flux by applying a synchronous function to each item
         */
        Flux.fromIterable(getUsers())
                .map(u -> toLowerCase(u.getName()))
                .doOnNext(System.out::println)
                .blockLast();
    }

    //A FlatMap divides an Observable into many singular Observables. Hence,
    // instead of sending each of the Observable values one by one, a
    // they are completed.
    @Test
    void streamOfFluxWithFlatmap() {
        Flux.fromIterable(getUsers())
                // .map(u -> getUserAcl(u.getName()))
                .flatMap(u -> Flux.just(toUpperCase(u.getName())))
                .doOnNext(d -> System.out.println(d))
                .blockLast();

    }

    // FlatMap does everything in parallel and then merges the results in whatever order
    @Test
    void streamOfFluxWithFlatmapOrderTest() {
        Flux.fromIterable(getUsers())
                .window(2)
                .flatMap(f -> f.map(u -> this.toUpperCase(u.getName())).subscribeOn(Schedulers.parallel()))
                .doOnNext(d -> System.out.println(d))
                .blockLast();

    }

    @Test
    void streamOfFluxWithFlatMapSequentialTest() {
        Flux.fromIterable(getUsers())
                .window(2)
                .flatMapSequential(f -> f.map(u -> this.toUpperCase(u.getName())).subscribeOn(Schedulers.parallel()))
                .doOnNext(d -> System.out.println(d))
                .blockLast();

    }

    //similar to a flatMap except that it keeps the order of the observables.
    @Test
    void streamOfFluxWithConcatMap() {
        Flux.fromIterable(getUsers())
                .window(2)
                .concatMap(f -> f.map(u -> this.toUpperCase(u.getName())).subscribeOn(Schedulers.parallel()))
                .doOnNext(d -> System.out.println(d))
                .blockLast();

    }

    //A SwitchMap flattens the source observable but only returns the last emitted single observable.
    @Test
    void streamOfFluxWithSwitchMap() {
        Flux.fromIterable(getUsers())
                .window(2)
                .switchMap(f -> f.map(u -> this.toUpperCase(u.getName())).subscribeOn(Schedulers.parallel()))
                .doOnNext(d -> System.out.println(d))
                .blockLast();
    }

    //combine stream without order.
    @Test
    void combineFlux() {
        Flux<String> flux1 = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("e", "f", "g").delayElements(Duration.ofSeconds(1));

        Flux<String> mergedFlux = Flux.merge(flux1, flux2);

        StepVerifier.create(mergedFlux.log()).expectNextCount(6).verifyComplete();
    }

    //combine stream with order with sync
    @Test
    void concatFlux() {
        Flux<String> flux1 = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("e", "f", "g").delayElements(Duration.ofSeconds(1));

        Flux<String> mergedFlux = Flux.merge(flux1, flux2);

        StepVerifier.create(mergedFlux.log()).expectNextCount(6).verifyComplete();
    }

    //combine stream with order
    @Test
    void zipFlux() {
        Flux<String> flux1 = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("e", "f", "g").delayElements(Duration.ofSeconds(1));

        Flux<Tuple2<String, String>> mergedFlux = Flux.zip(flux1, flux2);

        StepVerifier.create(mergedFlux.log()).expectNextCount(3).verifyComplete();
    }

    //combine stream with order
    @Test
    void zipWithFlux() {
        Flux<String> flux1 = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1));
        Flux<String> flux2 = Flux.just("e", "f", "g").delayElements(Duration.ofSeconds(1));

        Flux<Tuple2<String, String>> mergedFlux = flux1.zipWith(flux1);

        StepVerifier.create(mergedFlux.log()).expectNextCount(3).verifyComplete();
    }

    //Emit data from the beginning
    @Test
    public void coldPublisher() throws InterruptedException {
        Flux<String> flux = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1));

        flux.log().subscribe(it -> System.out.println("subscribe-1" + it));

        Thread.sleep(Duration.ofSeconds(1).toMillis());

        flux.log().subscribe(it -> System.out.println("subscribe-2" + it));
    }

    //Emit data from the beginning
    @Test
    public void hotPublisher() throws InterruptedException {
        ConnectableFlux<String> flux = Flux.just("a", "b", "c", "d", "e")
                .delayElements(Duration.ofSeconds(1))
                .publish();
        flux.connect();

        flux.log().subscribe(it -> System.out.println("subscribe-1" + it));

        Thread.sleep(Duration.ofSeconds(3).toMillis());

        flux.log().subscribe(it -> System.out.println("subscribe-2" + it));
        Thread.sleep(Duration.ofSeconds(6).toMillis());
    }

    @Test
    public void hotPublisher2() throws InterruptedException {
        Flux<String> flux = Flux.just("a", "b", "c", "d", "e")
                .delayElements(Duration.ofSeconds(1))
                .share();


        flux.log().subscribe(it -> System.out.println("subscribe-1" + it));

        Thread.sleep(Duration.ofSeconds(3).toMillis());

        flux.log().subscribe(it -> System.out.println("subscribe-2" + it));
        Thread.sleep(Duration.ofSeconds(6).toMillis());
    }

    @Test
    void streamOfFluxWithCreate() {
//        Flux<User> userFlux = getUsers().take(2).log();
//        StepVerifier.create(userFlux)
//                .expectNextCount(2)
//                .verifyComplete();
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
