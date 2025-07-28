package com.reservation.admin.application;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

@SpringBootTest
public class LoadBalancerTest {
	private final RestClient restClient = RestClient.create();
	private final String url = "http://localhost:8080/health";

	@Test
	public void loadBalanceTest() throws InterruptedException {
		int totalRequests = 100;
		int threadPoolSize = 10;

		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		CountDownLatch latch = new CountDownLatch(totalRequests);

		IntStream.range(0, totalRequests).forEach(i -> executor.submit(() -> {
			try {
				String response = restClient.get()
					.uri(url)
					.retrieve()
					.body(String.class);

				System.out.println("[" + i + "] Response: " + response);
			} finally {
				latch.countDown();
			}
		}));

		latch.await();
		executor.shutdown();
	}
}
