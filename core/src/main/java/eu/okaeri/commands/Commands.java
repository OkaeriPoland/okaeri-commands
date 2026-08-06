package eu.okaeri.commands;

import eu.okaeri.commands.annotation.Context;
import eu.okaeri.commands.handler.access.AccessHandler;
import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.commands.handler.completion.CompletionHandler;
import eu.okaeri.commands.handler.completion.NamedCompletionHandler;
import eu.okaeri.commands.handler.error.ErrorHandler;
import eu.okaeri.commands.handler.instance.InstanceCreatorHandler;
import eu.okaeri.commands.handler.result.ResultHandler;
import eu.okaeri.commands.handler.scheduling.SchedulingHandler;
import eu.okaeri.commands.handler.text.TextHandler;
import eu.okaeri.commands.handler.validation.ParameterValidationHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.meta.ContextMeta;
import eu.okaeri.commands.meta.ExecutorMeta;
import eu.okaeri.commands.meta.InvocationMeta;
import eu.okaeri.commands.meta.ServiceMeta;
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

    OkaeriCommands schedulingHandler(@NonNull SchedulingHandler schedulingHandler);

    ErrorHandler getErrorHandler();

    ResultHandler getResultHandler();

    TextHandler getTextHandler();

    MissingArgumentHandler getMissingArgumentHandler();

    AccessHandler getAccessHandler();

    CompletionHandler getCompletionHandler();

    InstanceCreatorHandler getInstanceCreatorHandler();

    SchedulingHandler getSchedulingHandler();

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

    /**
     * Base type of this platform's command sender, e.g. {@code CommandSender} on bukkit.
     * Any executor parameter assignable to it narrows the executor to matching senders.
     *
     * @return the sender base type, or null on platforms without a sender concept
     */
    default Class<?> getSenderType() {
        return null;
    }

    /**
     * Only the declared type decides this - an annotation cannot turn a parameter the sender could
     * never satisfy into a context parameter, or {@code @Context SomeService} would stop reaching
     * the {@link MissingArgumentHandler} and fail with a nonsensical "can only be executed by
     * SomeService" instead.
     *
     * @return true when the parameter is to be filled with the sender from the command data
     */
    default boolean isContextParameter(@NonNull Parameter param) {
        Class<?> senderType = this.getSenderType();
        return (senderType != null) && senderType.isAssignableFrom(param.getType());
    }

    /**
     * Whether a context parameter opts into the sender explicitly, as opposed to being narrowed by
     * its declared type alone. A required parameter is enforced even when the data carries no
     * sender; a type-derived one falls back to the {@link MissingArgumentHandler} instead.
     *
     * <p>Only consulted for parameters {@link #isContextParameter(Parameter)} already accepted.</p>
     *
     * @return true when the parameter is explicitly annotated as a context parameter
     */
    default boolean isContextRequired(@NonNull Parameter param) {
        return param.getAnnotation(Context.class) != null;
    }

    /**
     * @return true when the sender in the data satisfies every context parameter of the executor
     */
    default boolean allowContext(@NonNull ExecutorMeta executor, @NonNull CommandData data) {

        List<ContextMeta> contexts = executor.getContexts();
        if (contexts.isEmpty()) {
            return true;
        }

        // nothing to narrow against, let the invocation decide
        Object sender = data.get(CommandData.SENDER);
        if (sender == null) {
            return true;
        }

        return contexts.stream().allMatch(context -> context.accepts(sender));
    }

    /**
     * Checks every executor registered under the service's <i>label</i> — several services may share
     * one label, and a label is a single client-visible unit.
     *
     * @return true when at least one executor under the label accepts the sender
     */
    default boolean allowContext(@NonNull ServiceMeta service, @NonNull CommandData data) {
        return this.findByLabel(service.getLabel()).stream()
            .anyMatch(command -> this.allowContext(command.getExecutor(), data));
    }

    /**
     * Both gates every surface exposing an executor has to pass: permissions and sender narrowing.
     */
    default boolean allowExecutor(@NonNull ExecutorMeta executor, @NonNull Invocation invocation, @NonNull CommandData data) {
        return this.getAccessHandler().allowAccess(executor, invocation, data)
            && this.allowContext(executor, data);
    }

    /**
     * @see #allowExecutor(ExecutorMeta, Invocation, CommandData)
     */
    default boolean allowService(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data) {
        return this.allowService(service, invocation, data, true);
    }

    /**
     * @see #allowExecutor(ExecutorMeta, Invocation, CommandData)
     */
    default boolean allowService(@NonNull ServiceMeta service, @NonNull Invocation invocation, @NonNull CommandData data, boolean checkExecutors) {
        return this.getAccessHandler().allowAccess(service, invocation, data, checkExecutors)
            && this.allowContext(service, data);
    }

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
