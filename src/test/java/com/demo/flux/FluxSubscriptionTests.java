package com.demo.flux;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;


class FluxSubscriptionTests {

	@Test
	void streamOfFluxSubscriptionTest() {
		Flux.just("A", "B", "C")
				.log()
				.subscribe(System.out::println);
	}

	@Test
	void streamOfFluxSubscriptionWithError() {
		Flux.just("A", "B", "C")
				.concatWith(Flux.error(new RuntimeException("Opps! something went wrong")))
				.log()
				.subscribe(
						System.out::println,
						error -> System.out.println(error.getMessage()));
	}

	@Test
	void streamOfFluxSubscriptionWithErrorHandling() {
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
	void streamOfFluxSubscriptionWithTake() throws InterruptedException {
		Flux.interval(Duration.ofSeconds(1))
				.take(2)
				.log()
				.subscribe(
						System.out::println);
		Thread.sleep(5000);
	}

	@Test
	void streamOfFluxSubscriptionWithSpecifiedRequestByUsingSubscriberImpl() throws InterruptedException {
		Flux.range(1, 6)
				.log()
				.subscribe(new Subscriber<Integer>() {
					private long count = 0;
					private Subscription subscription;
					public final int PAGE_SIZE = 2;

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
		Thread.sleep(5000);
	}
}
