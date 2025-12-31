package com.ratelimiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RateLimiterDemo {
    public static void main(String[] args) throws InterruptedException {
        // Create a rate limiter with capacity of 10 tokens and refill rate of 5 tokens per second
        RateLimiterService rateLimiter = new RateLimiterService(10, 5);
        
        // Simulate concurrent requests from a single client
        String clientId = "client-1";
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        System.out.println("Starting simulation for " + clientId);
        System.out.println("Capacity: 10, Refill Rate: 5 tokens/sec");
        
        long startTime = System.currentTimeMillis();
        
        // Submit 20 tasks continuously
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                boolean allowed = rateLimiter.allowRequest(clientId);
                if (allowed) {
                    System.out.println("Request ALLOWED at " + (System.currentTimeMillis() - startTime) + "ms");
                } else {
                    System.out.println("Request DENIED at " + (System.currentTimeMillis() - startTime) + "ms");
                }
                try {
                    // Small random sleep to vary arrival times slightly
                    Thread.sleep((long) (Math.random() * 50));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // Wait for some time to allow refill and submit more requests
        Thread.sleep(2000); 
        System.out.println("\n--- After 2 seconds pause (should be refilled) ---");
        
        for (int i = 0; i < 10; i++) {
             executor.submit(() -> {
                boolean allowed = rateLimiter.allowRequest(clientId);
                if (allowed) {
                     System.out.println("Request ALLOWED at " + (System.currentTimeMillis() - startTime) + "ms");
                } else {
                     System.out.println("Request DENIED at " + (System.currentTimeMillis() - startTime) + "ms");
                }
             });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Simulation finished.");
    }
}
