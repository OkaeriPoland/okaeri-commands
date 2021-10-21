package eu.okaeri.commands.injector;

import eu.okaeri.commands.handler.argument.DefaultMissingArgumentHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;
import java.util.Optional;

@RequiredArgsConstructor
public class InjectorArgumentHandler extends DefaultMissingArgumentHandler {

    @NonNull
    private final Injector injector;

    @Override
    public Object resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

        Class<?> paramType = param.getType();
        String name = (param.getAnnotation(Inject.class) == null) ? "" : param.getAnnotation(Inject.class).value();

        Optional<?> injectable = this.injector.get(name, paramType);
        if (injectable.isPresent()) {
            return injectable.get();
        }

        return super.resolve(invocationContext, commandContext, command, param, index);
    }
}
