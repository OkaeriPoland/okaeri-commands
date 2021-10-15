package eu.okaeri.commands.handler.instance;

import lombok.NonNull;
import lombok.SneakyThrows;

public class DefaultInstanceCreatorHandler implements InstanceCreatorHandler {

    @Override
    @SneakyThrows
    public <T> T createInstance(@NonNull Class<T> clazz) {
        return clazz.newInstance();
    }
}
