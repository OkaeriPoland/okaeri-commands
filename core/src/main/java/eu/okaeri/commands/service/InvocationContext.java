package eu.okaeri.commands.service;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import lombok.Data;

@Data
public class InvocationContext {

    public static InvocationContext of(CommandMeta command, ExecutorMeta executor, String label, String args) {
        InvocationContext context = new InvocationContext();
        context.command = command;
        context.executor = executor;
        context.label = label;
        context.args = args;
        return context;
    }

    private CommandMeta command;
    private ExecutorMeta executor;

    private String label;
    private String args;
}