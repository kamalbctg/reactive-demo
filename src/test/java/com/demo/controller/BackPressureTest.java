package com.demo.controller;

import com.demo.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

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
    void getProductStreamWithBackPressure3() throws InterruptedException {
        WebClient client = WebClient.create("http://localhost:8080/");
        client.get()
                .uri("v1/products").accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(Product.class)
                .subscribeOn(Schedulers.parallel()).log()
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

        Thread.sleep(Duration.ofSeconds(2).toMillis());
    }
}