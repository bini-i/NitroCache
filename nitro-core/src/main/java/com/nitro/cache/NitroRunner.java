package com.nitro.cache;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NitroRunner {

	public static void main(String[] args) throws InterruptedException {
		
		int capacity  = 1024;
		
		NitroCache<String, Integer> cache = new NitroCache<>(capacity);
		
		int threadCount = 8;
		int incrementsPerThread = 100_000;
		String sharedKey = "counter";   // All threads hammer this ONE key to force collisions
		
		System.out.println("------ Starting Nitro Stress Test ------");
		System.out.println("Threads " + threadCount);
		System.out.println("Goal: " + (threadCount * incrementsPerThread) + " total increments");
		
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		
		long startTime = System.nanoTime();
		
		for(int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					for(int j = 0; j < incrementsPerThread; j++) {
						cache.incrementInteger(sharedKey);
//						Integer val = cache.get(sharedKey);
//						cache.put(sharedKey, (val == null ? 0 : val) + 1);
					}
				} finally {
					latch.countDown();
				}
			});
		}
		
		latch.await();      // wait for all threads to finish
		long duration = System.nanoTime() - startTime;
		
		Integer finalValue = cache.get(sharedKey);
		executor.shutdown();
		
		System.out.println("---- Results --------");
		System.out.println("Final Value: " + finalValue);
		System.out.println("Time: " + (duration/1_000_000) + "ms");
		
		if(finalValue == (threadCount * incrementsPerThread)) {
			System.out.println("SUCCESS: Atomic integrity maintained");
		}else {
			System.out.println("FAILURE: Data lost! Race condition detected.");
		}
	}

}


















