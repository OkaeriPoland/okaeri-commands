package eu.okaeri.commands.meta;

import eu.okaeri.commands.service.CommandException;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Data
public class InvocationMeta {

    public static InvocationMeta of(@NonNull Method method, Object[] call, @NonNull ServiceMeta service, @NonNull ExecutorMeta executor) {
        InvocationMeta meta = new InvocationMeta();
        meta.method = method;
        meta.call = call;
        meta.service = service;
        meta.executor = executor;
        return meta;
    }

    @SneakyThrows
    public Object callSneaky() {
        return this.call();
    }

    public Object call() throws CommandException {
        Method method = this.executor.getMethod();
        method.setAccessible(true);
        try {
            return method.invoke(this.service.getImplementor(), this.call);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new CommandException("Failed command invocation: " + this.getMethod(), exception);
        }
    }

    private Method method;
    private Object[] call;

    private ServiceMeta service;
    private ExecutorMeta executor;
}
