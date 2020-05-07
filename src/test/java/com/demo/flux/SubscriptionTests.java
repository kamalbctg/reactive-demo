package com.demo.flux;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.time.Duration;


class SubscriptionTests {

    @Test
    void subscriptionTest() {
        Flux.just("A", "B", "C")
                .log()
                .subscribe(System.out::println);
    }

    @Test
    void subscriptionErrorTest() {
        Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("Opps! something went wrong")))
                .log()
                .subscribe(
                        System.out::println,
                        error -> System.out.println(error.getMessage()));
    }

    @Test
    void subscriptionWithErrorReturn() {
        Flux.just("A", "B")
                .concatWith(Flux.error(new RuntimeException("Opps! something went wrong")))
                .concatWithValues("C")
                .onErrorReturn("K")
                .log()
                .subscribe(
                        System.out::println,
                        error -> System.out.println(error.getMessage()));
    }

    @Test
    void subscriptionWithBackPressure() throws InterruptedException {
        Flux.range(1, 10).delayElements(Duration.ofSeconds(1))
                .log()
                .subscribe(new Subscriber<Integer>() {
                    public final int PAGE_SIZE = 2;
                    private long count = 0;
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        subscription.request(PAGE_SIZE);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        count++;
                        if (count >= 2) {
                            count = 0;
                            subscription.request(PAGE_SIZE);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        Thread.sleep(11000);
    }
}
