package com.framework.security.services;

import org.springframework.stereotype.Service;

import com.framework.exceptions.TooManyRequestsException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;

@Service
public class DenialOfServiceProtection {
	private Bucket bucket;
	
	public DenialOfServiceProtection() {
		bucket = createNewBucket();
	}

	public Bucket createNewBucket() {
	    long capacity = 10;
	    Refill refill = Refill.greedy(capacity, Duration.ofMinutes(1));
	    Bandwidth limit = Bandwidth.classic(capacity, refill);
	    return Bucket.builder().addLimit(limit).build();
	}
	
	public boolean consumeBucket() {
		if(bucket.tryConsume(1)) {
			return true;
		}
		throw new TooManyRequestsException("User exceeded the request limit per minute");
	}
}
