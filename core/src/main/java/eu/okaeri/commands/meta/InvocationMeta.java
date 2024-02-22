package eu.okaeri.commands.meta;

import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Objects;

@Data
public class InvocationMeta {

    private Invocation invocation;
    private CommandData command;
    private ExecutorMeta executor;
    private Object[] call;

    public static InvocationMeta of(@NonNull Invocation invocation, @NonNull CommandData command, @NonNull ExecutorMeta executor, Object[] call) {
        InvocationMeta meta = new InvocationMeta();
        meta.invocation = invocation;
        meta.command = command;
        meta.executor = executor;
        meta.call = call;
        return meta;
    }

    @SneakyThrows
    public Object callSneaky() {
        return this.call();
    }

    public Object call() throws Exception {

        CommandService implementor = Objects.requireNonNull(this.getInvocation().getService()).getImplementor();
        implementor.preInvoke(this.getInvocation(), this.getCommand(), this);

        Method method = this.getExecutor().getMethod();
        method.setAccessible(true);

        Object result = method.invoke(implementor, this.call);
        return implementor.postInvoke(this.getInvocation(), this.getCommand(), this, result);
    }
}
