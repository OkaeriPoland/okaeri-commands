package eu.okaeri.commands.meta;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.annotation.NestedCommand;
import eu.okaeri.commands.service.CommandService;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ServiceMeta {

    public static ServiceMeta of(@NonNull Commands commands, ServiceMeta parent, @NonNull CommandService service) {

        Class<? extends CommandService> serviceClazz = service.getClass();
        Command descriptor = serviceClazz.getAnnotation(Command.class);
        if (descriptor == null) {
            throw new IllegalArgumentException("cannot create ServiceMeta from CommandService without @ServiceDescriptor annotation");
        }

        ServiceMeta meta = new ServiceMeta();
        meta.implementor = service;
        meta.label = commands.resolveText(getResultingLabel(parent, descriptor.label()));
        meta.originalLabel = commands.resolveText(descriptor.label());
        meta.patternPrefix = commands.resolveText(getResultingPatternPrefix(parent, descriptor.label()));
        meta.aliases = Arrays.stream(descriptor.aliases()).map(commands::resolveText).collect(Collectors.toList());
        meta.description = commands.resolveText(descriptor.description());

        meta.parent = parent;
        meta.nested = Arrays.stream(descriptor.nested())
                .map(NestedCommand::value)
                .collect(Collectors.toList());

        return meta;
    }

    private CommandService implementor;
    private String label;
    private String originalLabel;
    private String patternPrefix;
    private List<String> aliases;
    private String description;

    private ServiceMeta parent;
    private List<Class<? extends CommandService>> nested;

    private static String getResultingLabel(ServiceMeta parent, @NonNull String fallback) {
        String label = null;
        while (parent != null) {
            label = parent.getLabel();
            parent = parent.getParent();
        }
        return (label == null) ? fallback : label;
    }

    private static String getResultingPatternPrefix(ServiceMeta parent, @NonNull String originalLabel) {
        if (parent == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        parts.add(originalLabel);
        while (parent != null) {
            parts.add(parent.getOriginalLabel());
            parent = parent.getParent();
        }
        Collections.reverse(parts);
        return String.join(" ", parts.subList(1, parts.size()));
    }
}
