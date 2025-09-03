package eu.okaeri.commands.bungee.handler;

import eu.okaeri.commands.bungee.annotation.Async;
import eu.okaeri.commands.bungee.annotation.Sync;
import eu.okaeri.commands.handler.scheduling.SchedulingHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static eu.okaeri.commands.bungee.CommandsBungee.INVOKE_SYNC_WARN_TIME;

@RequiredArgsConstructor
public class BungeeSchedulingHandler implements SchedulingHandler {

    private final Map<Method, Boolean> isAsyncCacheMethod = new ConcurrentHashMap<>();
    private final Map<Class<? extends CommandService>, Boolean> isAsyncCacheService = new ConcurrentHashMap<>();

    private final Plugin plugin;

    @Override
    public void run(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Runnable runnable) {

        if (this.isAsync(invocation)) {
            this.plugin.getProxy().getScheduler().runAsync(this.plugin, runnable);
            return;
        }

        Instant start = Instant.now();
        runnable.run();
        Duration time = Duration.between(Instant.now(), start);

        if (time.compareTo(INVOKE_SYNC_WARN_TIME) > 0) {
            this.plugin.getLogger().warning(syncTimeWarn(data, invocation, time, "invoke"));
        }
    }

    protected boolean isAsync(@NonNull Invocation invocation) {

        if ((invocation.getCommand() == null) || (invocation.getService() == null)) {
            throw new IllegalArgumentException("Cannot use dummy context: " + invocation);
        }

        Class<? extends CommandService> serviceClass = invocation.getService().getImplementor().getClass();
        boolean serviceAsync = this.isAsyncCacheService.computeIfAbsent(serviceClass, this::isAsync);

        Method executorMethod = invocation.getCommand().getExecutor().getMethod();
        Boolean methodAsync = this.isAsyncCacheMethod.computeIfAbsent(executorMethod, this::isAsync);

        // method overrides
        if (methodAsync != null) {
            return methodAsync;
        }

        // defaults
        return serviceAsync;
    }

    @Nullable
    protected Boolean isAsync(@NonNull Method method) {

        Async methodAsync = method.getAnnotation(Async.class);
        Sync methodSync = method.getAnnotation(Sync.class);

        if ((methodAsync != null) && (methodSync != null)) {
            throw new RuntimeException("Cannot use @Async and @Sync annotations simultaneously: " + method);
        }

        if ((methodAsync == null) && (methodSync == null)) {
            return null;
        }

        return methodAsync != null;
    }

    protected boolean isAsync(@NonNull Class<? extends CommandService> service) {

        Async serviceAsync = service.getAnnotation(Async.class);
        Sync serviceSync = service.getAnnotation(Sync.class);

        if ((serviceAsync != null) && (serviceSync != null)) {
            throw new RuntimeException("Cannot use @Async and @Sync annotations simultaneously: " + service);
        }

        return serviceAsync != null;
    }

    public static String syncTimeWarn(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Duration duration, @NonNull String type) {

        if ((invocation.getService() == null) || (invocation.getCommand() == null)) {
            throw new IllegalArgumentException("Cannot use dummy context: " + invocation);
        }

        // my.package.MyCommand#my_method
        String implementorName = invocation.getService().getImplementor().getClass().getName();
        String methodName = invocation.getCommand().getExecutor().getMethod().getName();
        String signature = implementorName + "#" + methodName;

        // (cmd: mycmd params, context={sender=CraftPlayer{name=Player1}, some=value})
        String fullCommand = (invocation.getLabel() + " " + invocation.getArgs()).trim();
        String context = "(cmd: " + fullCommand + ", context: " + data.all() + ")";

        // main thread, 11 ms
        String thread = Thread.currentThread().getName();
        String durationText = duration.toMillis() + " ms";

        return signature + " " + context + " execution took " + durationText + "! [" + thread + "] [" + type + "]";
    }
}
