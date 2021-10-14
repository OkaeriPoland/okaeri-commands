package eu.okaeri.commands.service;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import lombok.Data;
import lombok.NonNull;

@Data
public class InvocationContext {

    public static InvocationContext of(@NonNull CommandMeta command, @NonNull String label, @NonNull String args) {
        InvocationContext context = of(label, args);
        context.command = command;
        return context;
    }

    public static InvocationContext of(@NonNull String label, @NonNull String args) {
        InvocationContext context = new InvocationContext();
        context.label = label;
        context.args = args;
        String[] argArr = args.trim().split(" ");
        context.lastArg = argArr[argArr.length - 1];
        context.openArgs = args.endsWith(" ");
        return context;
    }

    public static InvocationContext of(@NonNull ServiceMeta service, @NonNull String label, @NonNull String[] args) {
        InvocationContext context = of(label, String.join(" ", args));
        context.service = service;
        return context;
    }

    private CommandMeta command;
    private ServiceMeta service;
    private String label;
    private String args;
    private String lastArg;
    private boolean openArgs;

    public boolean isDummy() {
        return this.command == null;
    }

    public ServiceMeta getService() {
        if (this.service != null) {
            return this.service;
        }
        if (this.command == null) {
            return null;
        }
        return this.command.getService();
    }

    public ExecutorMeta getExecutor() {
        if (this.command == null) {
            return null;
        }
        return this.command.getExecutor();
    }

    public boolean isAsync() {
        return this.getExecutor().isAsync();
    }
}
