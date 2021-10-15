package eu.okaeri.commands;

import eu.okaeri.commands.handler.access.AccessHandler;
import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.commands.handler.completion.CompletionHandler;
import eu.okaeri.commands.handler.error.ErrorHandler;
import eu.okaeri.commands.handler.instance.InstanceCreatorHandler;
import eu.okaeri.commands.handler.result.ResultHandler;
import eu.okaeri.commands.handler.text.TextHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.resolver.TypeResolver;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public interface Commands {

    OkaeriCommands errorHandler(@NonNull ErrorHandler errorHandler);

    OkaeriCommands resultHandler(@NonNull ResultHandler resultHandler);

    OkaeriCommands textHandler(@NonNull TextHandler textHandler);

    OkaeriCommands missingArgumentHandler(@NonNull MissingArgumentHandler argumentHandler);

    OkaeriCommands accessHandler(@NonNull AccessHandler accessHandler);

    OkaeriCommands completionHandler(@NonNull CompletionHandler completionHandler);

    OkaeriCommands instanceCreatorHandler(@NonNull InstanceCreatorHandler creatorHandler);

    Commands registerCommand(@NonNull Class<? extends CommandService> clazz);

    Commands registerCommand(@NonNull CommandService service);

    Commands registerType(@NonNull TypeResolver typeResolver);

    Commands registerTypeExclusive(@NonNull Type removeAnyForType, @NonNull TypeResolver typeResolver);

    Commands registerType(@NonNull CommandsExtension typesPack);

    Commands registerExtension(@NonNull CommandsExtension extension);

    String resolveText(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull String text);

    Object resolveMissingArgument(@NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext, @NonNull CommandMeta command, @NonNull Parameter param, int i);

    List<CommandMeta> findByLabel(@NonNull String label);

    Optional<CommandMeta> findByLabelAndArgs(@NonNull String label, @NonNull String args);

    Optional<TypeResolver> findTypeResolver(@NonNull Type type);

    <T> T call(@NonNull String command) throws InvocationTargetException, IllegalAccessException;

    Optional<InvocationContext> invocationMatch(@NonNull String command);

    InvocationMeta invocationPrepare(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);

    List<String> complete(@NonNull List<CommandMeta> metas, @NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext);

    List<String> complete(@NonNull String command);

    void onRegister(@NonNull CommandMeta command);
}
