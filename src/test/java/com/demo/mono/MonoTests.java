package com.demo.mono;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


class MonoTests {

	@Test
	void emptyMonoTest() {
		Mono emptyMono = Mono.empty().log();
		StepVerifier.create(emptyMono)
				.verifyComplete();
	}

	@Test
	void monoTest() {
		Mono<String> monoTest = Mono.just("A")
				.log();
		StepVerifier.create(monoTest)
				.expectNext("A")
				.verifyComplete();
	}

	@Test
	void monoErrorTest() {
		Mono monoTest = Mono.error(new RuntimeException("Opps"))
				.log();
		StepVerifier.create(monoTest)
				.expectError()
				.verify();
	}
}
