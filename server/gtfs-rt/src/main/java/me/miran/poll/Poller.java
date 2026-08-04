package me.miran.poll;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Poller<T> {

    private final Object mutex = new Object();
    private final Supplier<T> supplier;
    private final Consumer<T> consumer;
    private final Duration delay;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<T> value = new AtomicReference<>(null);
    private final CompletableFuture<T> firstValueFuture = new CompletableFuture<>();
    private ScheduledFuture<?> handle;

    public Poller(String name, Supplier<T> supplier, Duration delay) {
        this(name, supplier, ignored -> {}, delay);
    }

    public Poller(String name, Supplier<T> supplier, Consumer<T> consumer, Duration delay) {
        this.supplier = supplier;
        this.consumer = consumer;
        this.delay = delay;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "poll-"+name);
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        synchronized (mutex) {
            if (handle != null) return;

            long delaySec = delay.getSeconds();
            if (delaySec <= 0) {
                delaySec = 1;
            }

            handle = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    var t = supplier.get();
                    firstValueFuture.complete(t);

                    value.set(t);
                    consumer.accept(t);
                } catch (Exception e) {
                    e.printStackTrace();
                    firstValueFuture.completeExceptionally(e);
                }
            }, 0, delaySec, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        synchronized (mutex) {
            if (handle == null) return;

            handle.cancel(false);
            handle = null;

            firstValueFuture.completeExceptionally(new InterruptedException());
        }
    }

    public T latestValue() {
        var result = value.get();
        if (result == null) {
            try {
                return firstValueFuture.get(2, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }


}
