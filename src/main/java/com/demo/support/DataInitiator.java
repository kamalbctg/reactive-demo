package com.demo.support;

import com.demo.repository.ProductRepository;
import com.demo.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DataInitiator implements CommandLineRunner {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReactiveMongoOperations reactiveMongoOperations;

    @Override
    public void run(String... args) throws Exception {
        Flux<Product> products = Flux.just(
                new Product(null, "Item-1", "Category-1"),
                new Product(null, "Item-2", "Category-1"),
                new Product(null, "Item-3", "Category-2"),
                new Product(null, "Item-4", "Category-2"),
                new Product(null, "Item-5", "Category-3"),
                new Product(null, "Item-6", "Category-3"),
                new Product(null, "Item-7", "Category-4"),
                new Product(null, "Item-8", "Category-4"),
                new Product(null, "Item-9", "Category-5"),
                new Product(null, "Item-10", "Category-5"));

        reactiveMongoOperations.collectionExists(Product.class)
                .flatMap(it -> {
                    if (it) {
                        return reactiveMongoOperations.dropCollection(Product.class);
                    } else {
                        return Mono.empty();
                    }
                })
                .thenMany(reactiveMongoOperations.createCollection(Product.class))
                .thenMany(products.flatMap(it -> productRepository.save(it)))
                .thenMany(productRepository.findAll())
                .log()
                .subscribe(
                        it -> System.out.println(it),
                        error -> System.out.println(error),
                        () -> System.out.println(" -- Database has been initialized"));

    }
}
