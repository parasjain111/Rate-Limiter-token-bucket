Designing a Rate Limiter in Java: The Token Bucket Algorithm
In the world of distributed systems and high-traffic applications, controlling the flow of requests is crucial. Rate limiting is a strategy used to limit the network traffic. It helps to prevent abuse and ensures that the system remains stable and responsive.

In this post, we'll explore how to implement a Rate Limiter in Java using the Token Bucket algorithm. We'll go from the core concept to a full thread-safe implementation.

What is the Token Bucket Algorithm?
The Token Bucket algorithm is simple yet powerful. Imagine a bucket that holds tokens:

Capacity: The bucket has a maximum capacity of tokens.
Refill: Tokens are added to the bucket at a fixed rate (e.g., 5 tokens per second).
Consumption: When a request comes in, it must obtain a token from the bucket.
If a token is available, it's removed, and the request is processed.
If no tokens are available, the request is denied (dropped or queued).
This algorithm allows for bursts of traffic up to the bucket's capacity while enforcing a long-term average rate.

Step 1: Core Implementation (
TokenBucket
)
Let's start with the heart of our rate limiter: the 
TokenBucket
 class.

We need to track:

capacity: Max tokens.
refillRate: How many tokens to add per second.
tokens: Current count.
lastRefillTimestamp: When we last added tokens.
Crucially, instead of a background thread adding tokens every second (which is inefficient), we'll lazily refill tokens whenever a request attempts to consume one.

public class TokenBucket {
    private final long capacity;
    private final long refillRate; // tokens per second
    private long tokens;
    private long lastRefillTimestamp;
    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }
    public synchronized boolean tryConsume(long tokensToConsume) {
        refill();
        if (tokens >= tokensToConsume) {
            tokens -= tokensToConsume;
            return true;
        }
        return false;
    }
    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTimestamp;
        long tokensToAdd = (elapsedTime * refillRate) / 1000;
        
        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}
Key Points:

Thread Safety: The synchronized keyword ensures that concurrent requests don't corrupt the token count.
Lazy Refill: We calculate how many tokens would have been added since the last check and update the count.
Step 2: The Service Layer (
RateLimiterService
)
In a real application, you likely have multiple clients or API keys, each needing its own rate limit. We can manage this with a service class.

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class RateLimiterService {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final long refillRate;
    public RateLimiterService(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }
    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(capacity, refillRate));
        return bucket.tryConsume(1);
    }
}
Using ConcurrentHashMap allows us to handle multiple clients concurrently without global locking.

Step 3: Verification
Does it work? Let's simulate a scenario:

Capacity: 10 tokens
Refill Rate: 5 tokens/second (1 token every 200ms)
Scenario: A client sends a burst of requests.
// ... inside main method ...
RateLimiterService rateLimiter = new RateLimiterService(10, 5);
String clientId = "user-1";
// Simulate a loop of requests
for (int i = 0; i < 20; i++) {
    boolean allowed = rateLimiter.allowRequest(clientId);
    System.out.println("Request " + (allowed ? "ALLOWED" : "DENIED"));
}
Result: The first 10 requests are allowed immediately (burst capacity). The 11th request is denied because the bucket is empty. Subsequent requests are only allowed as the bucket refills (approx every 200ms).

Conclusion
We've built a robust, thread-safe rate limiter in just a few dozen lines of Java. The Token Bucket algorithm is efficient and flexible, making it a great choice for protecting your APIs.

Next Steps:

Support mismatched refill rates (e.g., refill every minute).
Use Redis for a distributed rate limiter across multiple servers.
Happy Coding!
