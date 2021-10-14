package eu.okaeri.commands.injector;

import eu.okaeri.commands.handler.argument.DefaultMissingArgumentHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;
import java.util.Optional;

@RequiredArgsConstructor
public class CommandsInjectorArgumentHandler extends DefaultMissingArgumentHandler {

    @NonNull
    private final Injector injector;

    @Override
    public Object resolve(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

        Class<?> paramType = param.getType();
        String name = (param.getAnnotation(Inject.class) == null) ? "" : param.getAnnotation(Inject.class).value();

        Optional<? extends Injectable<?>> injectable = this.injector.getInjectable(name, paramType);
        if (injectable.isPresent()) {
            return injectable.get().getObject();
        }

        return super.resolve(commandContext, invocationContext, command, param, index);
    }
}
