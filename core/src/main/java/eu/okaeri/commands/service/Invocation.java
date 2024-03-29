package eu.okaeri.commands.service;

import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ServiceMeta;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Data
public class Invocation {

    private final CommandMeta command;
    private final ServiceMeta service;
    private final String label;
    private final String args;
    private final String lastArg;
    private final boolean openArgs;

    protected Invocation(CommandMeta command, ServiceMeta service, String label, String args) {
        this.command = command;
        this.service = service;
        this.label = label;
        this.args = args;
        String[] argArr = args.trim().split(" ");
        this.lastArg = argArr[argArr.length - 1];
        this.openArgs = args.endsWith(" ");
    }

    public static Invocation of(@NonNull CommandMeta command, @NonNull String label, @NonNull String args) {
        return new Invocation(command, null, label, String.join(" ", args));
    }

    public static Invocation of(@NonNull String label, @NonNull String args) {
        return new Invocation(null, null, label, args);
    }

    public static Invocation of(@NonNull ServiceMeta service, @NonNull String label, @NonNull String[] args) {
        return new Invocation(null, service, label, String.join(" ", args));
    }

    @Nullable
    public CommandMeta getCommand() {
        return this.command;
    }

    @Nullable
    public ServiceMeta getService() {
        if (this.service != null) {
            return this.service;
        }
        if (this.command == null) {
            return null;
        }
        return this.command.getService();
    }
}
