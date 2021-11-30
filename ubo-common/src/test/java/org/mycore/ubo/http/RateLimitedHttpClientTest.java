package org.mycore.ubo.http;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.RateLimiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RateLimitedHttpClientTest {

    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        this.client = new HttpClient() {
            @Override public Optional<CookieHandler> cookieHandler() {
                return Optional.empty();
            }

            @Override public Optional<Duration> connectTimeout() {
                return Optional.empty();
            }

            @Override public Redirect followRedirects() {
                return null;
            }

            @Override public Optional<ProxySelector> proxy() {
                return Optional.empty();
            }

            @Override public SSLContext sslContext() {
                return null;
            }

            @Override public SSLParameters sslParameters() {
                return null;
            }

            @Override public Optional<Authenticator> authenticator() {
                return Optional.empty();
            }

            @Override public Version version() {
                return null;
            }

            @Override public Optional<Executor> executor() {
                return Optional.empty();
            }

            @Override
            public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException, InterruptedException {
                return null;
            }

            @Override
            public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
                return CompletableFuture.completedFuture(null);
            }
        };
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getRate() {
        final RateLimitedHttpClient httpClient = RateLimitedHttpClient.newBuilder()
            .rateLimit(10, TimeUnit.MINUTES).build();
        assertEquals(10 / 60, httpClient.getRate(), 1d);
    }

    @Test
    public void send() throws IOException, InterruptedException {
        final RateLimitedHttpClient httpClient = new RateLimitedHttpClient(client, RateLimiter.create(1));
        final Instant start = Instant.now();
        final int runs = 5;
        for (int i = 0; i < runs; i++) {
            httpClient.send(HttpRequest.newBuilder().uri(URI.create("http://junit.test")).build(), null);
        }
        final Duration duration = Duration.between(start, Instant.now());
        assertTrue("Request should have taken at least " + (runs - 1) + " seconds.",
            duration.getSeconds() + 1 >= (runs - 1));
    }
}
