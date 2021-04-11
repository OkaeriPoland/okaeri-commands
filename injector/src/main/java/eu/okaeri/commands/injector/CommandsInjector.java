package eu.okaeri.commands.injector;

import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.adapter.WrappedCommandsAdapter;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.exception.InjectorException;

import java.lang.reflect.Parameter;
import java.util.Optional;

public class CommandsInjector extends WrappedCommandsAdapter {

    private final Injector injector;

    public static CommandsInjector of(CommandsAdapter adapter, Injector injector) {
        return new CommandsInjector(adapter, injector);
    }

    protected CommandsInjector(CommandsAdapter adapter, Injector injector) {
        super(adapter);
        if (injector == null) throw new IllegalArgumentException("injector cannot be null");
        this.injector = injector;
    }

    public <T> CommandsInjector registerInjectable(String name, T object, Class<T> type) throws InjectorException {
        this.injector.registerInjectable(name, object, type);
        return this;
    }

    public <T> CommandsInjector registerInjectable(String name, T object) throws InjectorException {
        this.injector.registerInjectable(name, object);
        return this;
    }

    public <T> CommandsInjector registerInjectable(T object) throws InjectorException {
        this.injector.registerInjectable(object);
        return this;
    }

    @Override
    public Object resolveMissingArgument(CommandContext commandContext, InvocationContext invocationContext, CommandMeta command, Parameter param, int i) {

        if (commandContext == null) throw new IllegalArgumentException("context cannot be null");
        if (command == null) throw new IllegalArgumentException("command cannot be null");
        if (param == null) throw new IllegalArgumentException("param cannot be null");

        Class<?> paramType = param.getType();
        String name = (param.getAnnotation(Inject.class) == null) ? "" : param.getAnnotation(Inject.class).value();

        Optional<? extends Injectable<?>> injectable = this.injector.getInjectable(name, paramType);
        if (injectable.isPresent()) {
            return injectable.get().getObject();
        }

        return super.resolveMissingArgument(commandContext, invocationContext, command, param, i);
    }

    @Override
    public <T extends CommandService> T createInstance(Class<T> clazz) {
        return this.injector.createInstance(clazz);
    }
}
