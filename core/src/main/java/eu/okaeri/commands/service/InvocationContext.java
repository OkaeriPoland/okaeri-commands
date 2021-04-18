package eu.okaeri.commands.service;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import lombok.Data;

@Data
public class InvocationContext {

    public static InvocationContext of(CommandMeta command, String label, String args) {
        InvocationContext context = new InvocationContext();
        context.command = command;
        context.label = label;
        context.args = args;
        return context;
    }

    private CommandMeta command;
    private String label;
    private String args;

    public ServiceMeta getService() {
        return this.command.getService();
    }

    public ExecutorMeta getExecutor() {
        return this.command.getExecutor();
    }

    public boolean isAsync() {
        return this.getExecutor().isAsync();
    }
}
