package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


class ControlFlowTests {

    @Test
    void takeTest() {
        Flux<String> flux1 = Flux.just("a", "b", "c");
        StepVerifier.create(flux1.take(2).log()).expectNextCount(2).verifyComplete();
    }

    @Test
    void limitRequestTest() {
        //Backpressure signals
        Flux<String> flux1 = Flux.just("a", "b", "c");
        StepVerifier.create(flux1.limitRequest(2).log()).expectNextCount(2).verifyComplete();
    }

    @Test
    void limitRateTest() {
        //Backpressure signals
        Flux<String> flux1 = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1)).subscribeOn(Schedulers.parallel());
        StepVerifier.create(flux1.limitRate(2).log()).expectNextCount(3).verifyComplete();
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
