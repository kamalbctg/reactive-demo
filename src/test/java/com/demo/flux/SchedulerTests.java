package com.demo.flux;

import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


class SchedulerTests {

    @Test
    void publishOnTest() {
        Scheduler schedulerA = Schedulers.newParallel("scheduler-a", 4);

        Flux.range(1, 3)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .publishOn(schedulerA)
                .flatMap(i ->
                {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .log()
                .blockLast();
    }


    @Test
    void multiplePublishOnTest() {
        Scheduler schedulerA = Schedulers.newParallel("scheduler-a", 4);
        Scheduler schedulerB = Schedulers.newParallel("scheduler-b", 4);

        Flux.range(1, 3)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .publishOn(schedulerA)
                .flatMap(i ->
                {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .publishOn(schedulerB)
                .flatMap(i ->
                {
                    System.out.println(String.format("Third map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .log()
                .blockLast();
    }

    @Test
    void subscribeOnTest() {
        Scheduler schedulerA = Schedulers.newParallel("scheduler-a", 4);

        Flux.range(1, 3)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .subscribeOn(schedulerA)
                .flatMap(i ->
                {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .log()
                .blockLast();
    }

    @Test
    void subscribeOnAndPublishOnTest() {
        Scheduler schedulerA = Schedulers.newParallel("scheduler-a", 4);
        Scheduler schedulerB = Schedulers.newParallel("scheduler-b", 4);

        Flux.range(1, 3)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .subscribeOn(schedulerA)
                .flatMap(i ->
                {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .publishOn(schedulerB)
                .flatMap(i ->
                {
                    System.out.println(String.format("Third map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .log()
                .blockLast();
    }

    @Test
    void publishOnAndSubscribeOnTest() {
        Scheduler schedulerA = Schedulers.newParallel("scheduler-a", 4);
        Scheduler schedulerB = Schedulers.newParallel("scheduler-b", 4);

        Flux.range(1, 3)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .publishOn(schedulerA)
                .flatMap(i ->
                {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .subscribeOn(schedulerB)
                .flatMap(i ->
                {
                    System.out.println(String.format("Third map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .log()
                .blockLast();
    }


    @Test
    void multipleSubscribeOnTest() {
        Scheduler schedulerA = Schedulers.newParallel("scheduler-a", 4);
        Scheduler schedulerB = Schedulers.newParallel("scheduler-b", 4);

        Flux.range(1, 3)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .subscribeOn(schedulerA)
                .flatMap(i ->
                {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .subscribeOn(schedulerB)
                .flatMap(i ->
                {
                    System.out.println(String.format("Third map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return Flux.just(i * 2);
                })
                .log()
                .blockLast();
    }

    @Test
    void nestedChain() {
        Scheduler schedulerA = Schedulers.newParallel("scheduler-a", 4);
        Scheduler schedulerB = Schedulers.newParallel("scheduler-b", 4);


        Flux.range(1, 5)
                .map(i -> {
                    System.out.println(String.format("First map - (%s), Thread: %s", i, Thread.currentThread().getName()));
                    return i;
                })
                .subscribeOn(schedulerA)
                .map(i -> {
                    System.out.println(String.format("Second map - (%s), Thread: %s", i, Thread.currentThread().getName()));

                    return Flux
                            .range(1, 2)
                            .map(j -> {
                                System.out.println(String.format("inner First map - (%s.%s), Thread: %s", i, j, Thread.currentThread().getName()));
                                return j;
                            })
                            .subscribeOn(schedulerB)
                            .map(j -> {
                                System.out.println(String.format("inner Second map - (%s.%s), Thread: %s", i, j, Thread.currentThread().getName()));
                                return "value " + j;
                            }).subscribe();
                })
                .blockLast();
    }

    @Test
    void ParallelFlux() {
        Flux.range(0, 3)
                .parallel()
                .runOn(Schedulers.parallel())
                .log()
                .sequential()
                .blockLast();
    }
}
