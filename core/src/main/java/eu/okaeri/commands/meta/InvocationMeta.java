package eu.okaeri.commands.meta;

import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

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

    public Object call() throws Exception {
        Method method = this.executor.getMethod();
        method.setAccessible(true);
        return method.invoke(this.service.getImplementor(), this.call);
    }

    private Method method;
    private Object[] call;

    private ServiceMeta service;
    private ExecutorMeta executor;
}
