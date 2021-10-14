package eu.okaeri.commands;

import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.registry.CommandsRegistry;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.CommandsTypes;
import eu.okaeri.commands.type.CommandsTypesPack;
import eu.okaeri.commands.type.resolver.TypeResolver;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

public interface Commands {

    CommandsRegistry getRegistry();

    CommandsTypes getTypes();

    Commands register(@NonNull Class<? extends CommandService> clazz);

    Commands register(@NonNull CommandService service);

    Commands register(@NonNull TypeResolver typeResolver);

    Commands register(@NonNull CommandsTypesPack typesPack);

    <T> T call(@NonNull String command) throws InvocationTargetException, IllegalAccessException;

    Optional<InvocationContext> invocationMatch(@NonNull String command);

    InvocationMeta invocationPrepare(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);

    List<String> complete(@NonNull String command);
}
