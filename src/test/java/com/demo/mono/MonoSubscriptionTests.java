package com.demo.mono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


class MonoSubscriptionTests {

	@Test
	void monoWithJust() {
		 Mono.just("A")
				.log()
		 .subscribe(System.out::println);

	}

	@Test
	void monoWithError() {
		Mono.error(new RuntimeException("Opps!! Something went wrong"))
				.log()
                .subscribe(null, error -> System.out.println(error.getMessage()));
	}

}
