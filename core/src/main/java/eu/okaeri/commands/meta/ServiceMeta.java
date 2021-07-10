package eu.okaeri.commands.meta;

import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.service.CommandService;
import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;

@Data
public class ServiceMeta {

    public static ServiceMeta of(@NonNull CommandService service) {

        Class<? extends CommandService> serviceClazz = service.getClass();
        ServiceDescriptor descriptor = serviceClazz.getAnnotation(ServiceDescriptor.class);
        if (descriptor == null) {
            throw new IllegalArgumentException("cannot create ServiceMeta from CommandService without @ServiceDescriptor annotation");
        }

        ServiceMeta meta = new ServiceMeta();
        meta.implementor = service;
        meta.label = descriptor.label();
        meta.aliases = Arrays.asList(descriptor.aliases());
        meta.description = descriptor.description();
        return meta;
    }

    private CommandService implementor;
    private String label;
    private List<String> aliases;
    private String description;
}
