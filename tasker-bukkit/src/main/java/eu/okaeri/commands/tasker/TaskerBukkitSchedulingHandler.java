package eu.okaeri.commands.tasker;

import eu.okaeri.commands.bukkit.handler.BukkitSchedulingHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.tasker.annotation.Chain;
import eu.okaeri.tasker.bukkit.BukkitTasker;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskerBukkitSchedulingHandler extends BukkitSchedulingHandler {

    protected final Map<Method, String> chainCache = new ConcurrentHashMap<>();

    protected final BukkitTasker tasker;

    public TaskerBukkitSchedulingHandler(@NonNull BukkitTasker tasker) {
        super(tasker.getPlugin());
        this.tasker = tasker;
    }

    @Override
    protected void runSync(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Runnable runnable) {

        if (invocation.getCommand() == null) {
            super.runSync(data, invocation, runnable);
            return;
        }

        Method method = invocation.getCommand().getExecutor().getMethod();
        Chain chain = method.getAnnotation(Chain.class);

        if (chain == null) {
            super.runSync(data, invocation, runnable);
            return;
        }

        throw new RuntimeException("@Sync methods can't use @Chain. Forgot about @Async? (" + method + ")");
    }

    @Override
    protected void runAsync(@NonNull CommandData data, @NonNull Invocation invocation, @NonNull Runnable runnable) {

        if (invocation.getCommand() == null) {
            this.tasker.submit(runnable);
            return;
        }

        Method method = invocation.getCommand().getExecutor().getMethod();
        String chain = this.chainCache.computeIfAbsent(method, this::getChain);

        if (chain == null) {
            this.tasker.submit(runnable);
            return;
        }

        this.tasker.newSharedChain(chain).run(runnable).execute();
    }

    protected @Nullable String getChain(@NonNull Method method) {

        Chain chain = method.getAnnotation(Chain.class);
        if (chain != null) {
            return "".equals(chain.value()) ? null : chain.value();
        }

        Chain classChain = method.getDeclaringClass().getAnnotation(Chain.class);
        if (classChain != null) {
            return "".equals(classChain.value()) ? null : classChain.value();
        }

        return null;
    }
}
