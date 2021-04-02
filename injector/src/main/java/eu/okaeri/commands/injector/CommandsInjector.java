package eu.okaeri.commands.injector;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.adapter.WrappedCommandsAdapter;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;

import java.lang.reflect.Parameter;
import java.util.Optional;

public class CommandsInjector extends WrappedCommandsAdapter {

    private final Injector injector;

    public CommandsInjector(CommandsAdapter adapter, Injector injector) {
        super(adapter);
        this.injector = injector;
    }

    @Override
    public Object resolveMissingArgument(CommandContext context, CommandMeta command, Parameter param, int i) {

        Class<?> paramType = param.getType();
        String name = (param.getAnnotation(Inject.class) != null) ? param.getAnnotation(Inject.class).value() : "";

        Optional<? extends Injectable<?>> injectable = this.injector.getInjectable(name, paramType);
        if (injectable.isPresent()) {
            return injectable.get().getObject();
        }

        return super.resolveMissingArgument(context, command, param, i);
    }

    @Override
    public <T extends CommandService> T createInstance(Class<T> clazz) {
        return this.injector.createInstance(clazz);
    }
}
