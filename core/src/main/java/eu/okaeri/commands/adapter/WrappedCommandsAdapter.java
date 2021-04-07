package eu.okaeri.commands.adapter;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import lombok.Getter;

import java.lang.reflect.Parameter;

@Getter
public class WrappedCommandsAdapter extends CommandsAdapter {

    private final CommandsAdapter adapter;

    public WrappedCommandsAdapter(CommandsAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Commands getCore() {
        return this.adapter.getCore();
    }

    @Override
    public void setCore(Commands core) {
        this.adapter.setCore(core);
    }

    @Override
    public String resolveText(CommandContext context, String text) {
        return this.adapter.resolveText(context, text);
    }

    @Override
    public Object resolveMissingArgument(CommandContext context, CommandMeta command, Parameter param, int i) {
        return this.adapter.resolveMissingArgument(context, command, param, i);
    }

    @Override
    public <T extends CommandService> T createInstance(Class<T> clazz) {
        return this.adapter.createInstance(clazz);
    }

    @Override
    public void onRegister(CommandMeta command) {
        this.adapter.onRegister(command);
    }
}
