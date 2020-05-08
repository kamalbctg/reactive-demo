package com.demo.flux;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Slf4j
public class TransactionTest {
    @Test
    void subscriptionThenManyTest() throws InterruptedException {
        Mono<Void> then = Mono.subscriberContext().doOnNext(it -> it.get(String.class)).then();
        Flux.usingWhen(
                Transaction.beginTransaction(),
                transaction -> transaction.insertRows(Flux.just("A", "B", "C")),
                Transaction::commit,
                Transaction::rollback
        ).subscribe(
                d -> log.info("onNext: {}", d),
                e -> log.info("onError: {}", e.getMessage()),
                () -> log.info("onComplete")
        );

        Thread.sleep(Duration.ofSeconds(3).toMillis());
    }

    public static class Transaction {
        private static final Random random = new Random();
        private final int id;

        public Transaction(int id) {
            this.id = id;
            log.info("[T: {}] created", id);
        }

        public static Mono<Transaction> beginTransaction() {
            return Mono.defer(() ->
                    Mono.just(new Transaction(random.nextInt(1000))));
        }

        public Flux<String> insertRows(Publisher<String> rows) {
            return Flux.from(rows)
                    .delayElements(Duration.ofMillis(100))
                    .flatMap(r -> {
                        if (random.nextInt(10) < 2) {
                            //return Mono.error(new RuntimeException("Error: " + r));
                            return Mono.just(r);
                        } else {
                            return Mono.just(r);
                        }
                    });
        }

        public Mono<Void> commit() {
            return Mono.defer(() -> {
                log.info("[T: {}] commit", id);
//                if (random.nextBoolean()) {
//                    return Mono.empty();
//                } else {
//                    return Mono.error(new RuntimeException("Conflict"));
//                }
                return Mono.empty();
            });
        }

        public Mono<Void> rollback() {
            return Mono.defer(() -> {
                log.info("[T: {}] rollback", id);
                if (random.nextBoolean()) {
                    return Mono.empty();
                } else {
                    return Mono.error(new RuntimeException("Conn error"));
                }
            });
        }
    }
}
