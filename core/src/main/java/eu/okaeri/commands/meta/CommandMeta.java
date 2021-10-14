package eu.okaeri.commands.meta;

import eu.okaeri.commands.service.CommandService;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CommandMeta {

    public static List<CommandMeta> of(@NonNull CommandService service, @NonNull ServiceMeta serviceMeta, @NonNull Method method) {
        return ExecutorMeta.of(serviceMeta, method).stream()
                .map(meta -> {
                    CommandMeta command = new CommandMeta();
                    command.service = serviceMeta;
                    command.executor = meta;
                    return command;
                }).collect(Collectors.toList());
    }

    private ServiceMeta service;
    private ExecutorMeta executor;

    public boolean isLabelApplicable(@NonNull String label) {
        return label.equals(this.service.getLabel()) || this.service.getAliases().contains(label);
    }
}
