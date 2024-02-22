package eu.okaeri.commands;

import eu.okaeri.commands.handler.access.AccessHandler;
import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.commands.handler.completion.CompletionHandler;
import eu.okaeri.commands.handler.completion.NamedCompletionHandler;
import eu.okaeri.commands.handler.error.ErrorHandler;
import eu.okaeri.commands.handler.instance.InstanceCreatorHandler;
import eu.okaeri.commands.handler.result.ResultHandler;
import eu.okaeri.commands.handler.text.TextHandler;
import eu.okaeri.commands.handler.validation.ParameterValidationHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.SimpleTypeResolverAdapter;
import eu.okaeri.commands.type.resolver.TypeResolver;
import lombok.NonNull;

import java.io.Closeable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Commands extends Closeable {

    OkaeriCommands errorHandler(@NonNull ErrorHandler errorHandler);

    OkaeriCommands resultHandler(@NonNull ResultHandler resultHandler);

    OkaeriCommands textHandler(@NonNull TextHandler textHandler);

    OkaeriCommands missingArgumentHandler(@NonNull MissingArgumentHandler argumentHandler);

    OkaeriCommands accessHandler(@NonNull AccessHandler accessHandler);

    OkaeriCommands completionHandler(@NonNull CompletionHandler completionHandler);

    OkaeriCommands instanceCreatorHandler(@NonNull InstanceCreatorHandler creatorHandler);

    OkaeriCommands parameterValidationHandler(@NonNull ParameterValidationHandler validationHandler);

    ErrorHandler getErrorHandler();

    ResultHandler getResultHandler();

    TextHandler getTextHandler();

    MissingArgumentHandler getMissingArgumentHandler();

    AccessHandler getAccessHandler();

    CompletionHandler getCompletionHandler();

    InstanceCreatorHandler getInstanceCreatorHandler();

    Commands registerCommand(@NonNull Class<? extends CommandService> clazz);

    Commands registerCommand(@NonNull CommandService service);

    Commands registerType(@NonNull TypeResolver typeResolver);

    <T> Commands registerType(@NonNull Class<T> type, @NonNull Function<String, T> consumer);

    <T> Commands registerType(@NonNull Class<T> type, @NonNull SimpleTypeResolverAdapter<T> adapter);

    Commands registerTypeExclusive(@NonNull Type removeAnyForType, @NonNull TypeResolver typeResolver);

    Commands registerType(@NonNull CommandsExtension typesPack);

    Commands registerExtension(@NonNull CommandsExtension extension);

    Commands registerCompletion(@NonNull String name, @NonNull NamedCompletionHandler handler, boolean auto);

    default Commands registerCompletion(@NonNull String name, @NonNull NamedCompletionHandler handler) {
        return this.registerCompletion(name, handler, true);
    }

    Commands registerCompletion(@NonNull String name, @NonNull Supplier<Stream<String>> streamHandler);

    Commands registerCompletion(@NonNull String name, @NonNull Function<CommandData, Stream<String>> streamHandler);

    Commands registerCompletion(@NonNull Class<?> type, @NonNull NamedCompletionHandler handler, boolean auto);

    default Commands registerCompletion(@NonNull Class<?> type, @NonNull NamedCompletionHandler handler) {
        return this.registerCompletion(type, handler, true);
    }

    Commands registerCompletion(@NonNull Class<?> type, @NonNull Supplier<Stream<String>> streamHandler);

    Commands registerCompletion(@NonNull Class<?> type, @NonNull Function<CommandData, Stream<String>> streamHandler);

    String resolveText(@NonNull String text);

    String resolveText(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull String text);

    Object resolveMissingArgument(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int i);

    List<CommandMeta> findByLabel(@NonNull String label);

    Optional<CommandMeta> findByLabelAndArgs(@NonNull String label, @NonNull String args);

    Optional<TypeResolver> findTypeResolver(@NonNull Type type);

    <T> T call(@NonNull String command) throws Exception;

    Optional<Invocation> invocationMatch(@NonNull String command);

    InvocationMeta invocationPrepare(@NonNull Invocation invocation, @NonNull CommandData data);

    List<String> complete(@NonNull List<CommandMeta> metas, @NonNull Invocation invocation, @NonNull CommandData data);

    List<String> complete(@NonNull String command);

    void onRegister(@NonNull CommandMeta command);
}
