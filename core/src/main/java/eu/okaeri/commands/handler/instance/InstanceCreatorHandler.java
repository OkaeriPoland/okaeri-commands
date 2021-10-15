package eu.okaeri.commands.handler.instance;

import lombok.NonNull;

public interface InstanceCreatorHandler {
    <T> T createInstance(@NonNull Class<T> clazz);
}
