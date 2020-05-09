package com.demo.controller;

import com.demo.model.Product;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@ImportResource("application.properties")
class BackPressureTest {


    @Test
    void getProductStreamWithBackPressure() throws InterruptedException {
        WebClient client = WebClient.create("http://localhost:8080/");
        client.get()
                .uri("v1/products").accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(Product.class)
                .subscribe(
                        p -> System.out.println(">>>>" + p.getName()),
                        err -> System.out.println(err),
                        () -> System.out.println("All data has been recieved"));

        Thread.sleep(Duration.ofSeconds(5).toMillis());
    }

    @Test
    void getProductStreamWithBackPressure2() throws InterruptedException {
        WebClient client = WebClient.create("http://localhost:8080/");
        client.get()
                .uri("v1/products").accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(Product.class).log()
                .subscribe(new Subscriber<Product>() {
                    private Subscription subscription;
                    private Integer count = 0;
                    private Integer limit = 3;

                    @Override
                    public void onNext(Product t) {
                        count++;
                        if (count >= limit) {
                            count = 0;
                            subscription.request(limit);
                        }
                        System.out.println(">>" + t.getName());
                    }

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(limit);
                    }

                    @Override
                    public void onError(Throwable t) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onComplete() {
                        // TODO Auto-generated method stub
                    }
                });

        Thread.sleep(Duration.ofSeconds(1).toMillis());
    }

    @Test
    void getProductStreamWithBackPressureOnparallel() throws InterruptedException {
        WebClient client = WebClient.create("http://localhost:8080/");
        client.get()
                .uri("v1/products").accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(Product.class)
                .subscribeOn(Schedulers.parallel()).log()
                .onBackpressureError()
                .subscribe(new Subscriber<Product>() {
                    private Subscription subscription;
                    private Integer count = 0;
                    private Integer limit = 3;

                    @Override
                    public void onNext(Product t) {
                        count++;
                        if (count >= limit) {
                            count = 0;
                            subscription.request(limit);
                        }
                        System.out.println(">>" + t.getName());
                    }

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(limit);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        Thread.sleep(Duration.ofSeconds(2).toMillis());
    }


    @Test
    public void testBackPressure() throws  Exception{
        Flux<Integer> numberGenerator = Flux.create(x -> {
            System.out.println("Requested Events :"+x.requestedFromDownstream());
            int number = 1;
            while(number < 100) {
                x.next(number);
                number++;
            }
            x.complete();
        }, FluxSink.OverflowStrategy.ERROR);

        CountDownLatch latch = new CountDownLatch(1);
        numberGenerator.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(1);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(value);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            protected void hookOnComplete() {
                latch.countDown();
            }
        });
        latch.await(1L, TimeUnit.SECONDS);
    }

    @Test
    public void testBackPressureDrop() throws  Exception{
        Flux<Integer> numberGenerator = Flux.create(x -> {
            System.out.println("Requested Events :"+x.requestedFromDownstream());
            int number = 1;
            while(number < 100) {
                x.next(number);
                number++;
            }
            x.complete();
        });

        CountDownLatch latch = new CountDownLatch(1);
        numberGenerator
                //.onBackpressureDrop(x -> System.out.println("Dropped :"+x))
                .onBackpressureBuffer(2,x -> System.out.println("Dropped :"+x), BufferOverflowStrategy.DROP_LATEST)
                .subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(1);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(value);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                throwable.printStackTrace();
                latch.countDown();
            }

            @Override
            protected void hookOnComplete() {
                latch.countDown();
            }
        });
        latch.await(2l , TimeUnit.SECONDS);
    }
}