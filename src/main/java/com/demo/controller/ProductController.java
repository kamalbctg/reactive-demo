package com.demo.controller;

import com.demo.model.Product;
import com.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
@RequestMapping("v1")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public Flux<Product> getProducts() {
        return productRepository.findAll();
    }

    @GetMapping(value = "/products/stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public Flux<Product> getProductsAsStream() {
        //return productRepository.findAll().publishOn(Schedulers.parallel()).delayElements(Duration.ofSeconds(1));
        return productRepository.findAll().publishOn(Schedulers.parallel());
    }

    @GetMapping("/products/{id}")
    public Mono<Product> getProduct(@PathVariable("id") String id) {
        return productRepository.findById(id);
    }

    @PostMapping("/products")
    public Mono<Product> save(@RequestBody Product product){
        return productRepository.save(product);
    }

    @PutMapping("/products")
    public Mono<Product> update(@RequestBody Product product){
        return productRepository.save(product);
    }

    @DeleteMapping("/products/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return productRepository.deleteById(id);
    }
}
