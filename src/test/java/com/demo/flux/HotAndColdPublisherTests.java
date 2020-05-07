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


class HotAndColdPublisherTests {

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
