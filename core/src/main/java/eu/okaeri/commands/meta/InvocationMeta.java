package eu.okaeri.commands.meta;

import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Data
public class InvocationMeta {

    public static InvocationMeta of(Method method, Object[] call, ServiceMeta service, ExecutorMeta executor) {
        InvocationMeta meta = new InvocationMeta();
        meta.method = method;
        meta.call = call;
        meta.service = service;
        meta.executor = executor;
        return meta;
    }

    @SneakyThrows
    public Object callSneaky() {
        return this.executor.getMethod().invoke(this.service.getImplementor(), this.call);
    }

    public Object call() throws InvocationTargetException, IllegalAccessException {
        return this.executor.getMethod().invoke(this.service.getImplementor(), this.call);
    }

    private Method method;
    private Object[] call;

    private ServiceMeta service;
    private ExecutorMeta executor;
}
