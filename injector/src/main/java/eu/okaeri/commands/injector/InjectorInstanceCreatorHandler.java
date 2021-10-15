package eu.okaeri.commands.injector;

import eu.okaeri.commands.handler.instance.InstanceCreatorHandler;
import eu.okaeri.injector.Injector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InjectorInstanceCreatorHandler implements InstanceCreatorHandler {

    @NonNull
    private final Injector injector;

    @Override
    public <T> T createInstance(@NonNull Class<T> clazz) {
        return this.injector.createInstance(clazz);
    }
}
