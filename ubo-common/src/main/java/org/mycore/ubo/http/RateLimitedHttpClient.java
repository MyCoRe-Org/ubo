package org.mycore.ubo.http;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.mycore.common.MCRException;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimitedHttpClient extends HttpClient {
    private final HttpClient client;

    private final RateLimiter rateLimiter;

    RateLimitedHttpClient(HttpClient client, RateLimiter rateLimiter) {
        this.client = client;
        this.rateLimiter = rateLimiter;
    }

    public double getRate() {
        return rateLimiter.getRate();
    }

    @Override public Optional<CookieHandler> cookieHandler() {
        return client.cookieHandler();
    }

    @Override public Optional<Duration> connectTimeout() {
        return client.connectTimeout();
    }

    @Override public Redirect followRedirects() {
        return client.followRedirects();
    }

    @Override public Optional<ProxySelector> proxy() {
        return client.proxy();
    }

    @Override public SSLContext sslContext() {
        return client.sslContext();
    }

    @Override public SSLParameters sslParameters() {
        return client.sslParameters();
    }

    @Override public Optional<Authenticator> authenticator() {
        return client.authenticator();
    }

    @Override public Version version() {
        return client.version();
    }

    @Override public Optional<Executor> executor() {
        return client.executor();
    }

    @Override public <T> HttpResponse<T> send(HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        try {
            return sendAsync(request, responseBodyHandler).get();
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            }
            throw new IOException(cause);
        }
    }

    @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        return sendAsync(request, responseBodyHandler, null);
    }

    @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return acquireRequestPermission(request)
            .thenCompose(waitTime -> client.sendAsync(request, responseBodyHandler, pushPromiseHandler));
    }

    private CompletableFuture<Double> acquireRequestPermission(HttpRequest request) {
        final Optional<Duration> connectionTimeOut = request.timeout()
            .or(this::connectTimeout);
        final Executor executor = executor()
            .orElseGet(ForkJoinPool::commonPool);
        return connectionTimeOut.map(duration -> CompletableFuture.supplyAsync(() -> {
            final Instant start = Instant.now();
            if (rateLimiter.tryAcquire(duration)) {
                return Duration.between(start, Instant.now())
                    .get(ChronoUnit.MILLIS) / (double) 1000;
            }
            throw new MCRException("Timeout");
        }, executor)).orElseGet(() -> CompletableFuture.supplyAsync(rateLimiter::acquire, executor));
    }

    public static Builder newBuilder() {
        return new BuilderImpl();
    }

    public interface Builder extends HttpClient.Builder {
        Builder rateLimit(int rate, TimeUnit unit);

        @Override RateLimitedHttpClient build();
    }

    private static class BuilderImpl implements Builder {
        private final HttpClient.Builder builder = HttpClient.newBuilder();

        private RateLimiter rateLimiter;

        @Override public HttpClient.Builder cookieHandler(CookieHandler cookieHandler) {
            builder.cookieHandler(cookieHandler);
            return this;
        }

        @Override public HttpClient.Builder connectTimeout(Duration duration) {
            builder.connectTimeout(duration);
            return this;
        }

        @Override public HttpClient.Builder sslContext(SSLContext sslContext) {
            builder.sslContext(sslContext);
            return this;
        }

        @Override public HttpClient.Builder sslParameters(SSLParameters sslParameters) {
            builder.sslParameters(sslParameters);
            return this;
        }

        @Override public HttpClient.Builder executor(Executor executor) {
            builder.executor(executor);
            return this;
        }

        @Override public HttpClient.Builder followRedirects(Redirect policy) {
            builder.followRedirects(policy);
            return this;
        }

        @Override public HttpClient.Builder version(Version version) {
            builder.version(version);
            return this;
        }

        @Override public HttpClient.Builder priority(int priority) {
            builder.priority(priority);
            return this;
        }

        @Override public HttpClient.Builder proxy(ProxySelector proxySelector) {
            builder.proxy(proxySelector);
            return this;
        }

        @Override public HttpClient.Builder authenticator(Authenticator authenticator) {
            builder.authenticator(authenticator);
            return this;
        }

        @Override public RateLimitedHttpClient build() {
            if (rateLimiter == null) {
                throw new IllegalStateException("No rate limit defined!");
            }
            return new RateLimitedHttpClient(builder.build(), rateLimiter);
        }

        @Override public Builder rateLimit(int rate, TimeUnit unit) {
            rateLimiter = RateLimiter.create(rate / (double) unit.toSeconds(1));
            return this;
        }
    }
}
