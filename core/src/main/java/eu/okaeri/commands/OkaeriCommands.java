package eu.okaeri.commands;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.Label;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.exception.NoSuchCommandException;
import eu.okaeri.commands.handler.access.AccessHandler;
import eu.okaeri.commands.handler.access.DefaultAccessHandler;
import eu.okaeri.commands.handler.argument.DefaultMissingArgumentHandler;
import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.commands.handler.completion.CompletionHandler;
import eu.okaeri.commands.handler.completion.DefaultCompletionHandler;
import eu.okaeri.commands.handler.completion.NamedCompletionHandler;
import eu.okaeri.commands.handler.error.DefaultErrorHandler;
import eu.okaeri.commands.handler.error.ErrorHandler;
import eu.okaeri.commands.handler.instance.DefaultInstanceCreatorHandler;
import eu.okaeri.commands.handler.instance.InstanceCreatorHandler;
import eu.okaeri.commands.handler.result.DefaultResultHandler;
import eu.okaeri.commands.handler.result.ResultHandler;
import eu.okaeri.commands.handler.text.DefaultTextHandler;
import eu.okaeri.commands.handler.text.TextHandler;
import eu.okaeri.commands.handler.validation.DefaultParameterValidationHandler;
import eu.okaeri.commands.handler.validation.ParameterValidationHandler;
import eu.okaeri.commands.handler.validation.ValidationResult;
import eu.okaeri.commands.meta.*;
import eu.okaeri.commands.meta.pattern.PatternMeta;
import eu.okaeri.commands.meta.pattern.element.PatternElement;
import eu.okaeri.commands.meta.pattern.element.StaticElement;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.CommandException;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.DefaultCommandsTypes;
import eu.okaeri.commands.type.resolver.SimpleTypeResolver;
import eu.okaeri.commands.type.resolver.SimpleTypeResolverAdapter;
import eu.okaeri.commands.type.resolver.TypeResolver;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Data
public class OkaeriCommands implements Commands {

    public static final String SEPARATOR = ";";

    protected static final Comparator<CommandMeta> META_COMPARATOR = Comparator
        .comparing((CommandMeta meta) -> {
            List<PatternElement> elements = meta.getExecutor().getPattern().getElements();
            return elements.size();
        }, Comparator.reverseOrder())
        .thenComparing((CommandMeta meta) -> {
            List<PatternElement> elements = meta.getExecutor().getPattern().getElements();
            return elements.stream().filter(element -> element instanceof StaticElement).count();
        }, Comparator.reverseOrder());

    protected final Map<String, List<CommandMeta>> registeredCommandsByLabel = new ConcurrentHashMap<>();
    protected final List<TypeResolver> typeResolvers = new ArrayList<>();
    protected final Map<Type, TypeResolver> resolverCache = new ConcurrentHashMap<>();
    protected final Map<String, NamedCompletionHandler> namedCompletionHandlers = new ConcurrentHashMap<>();
    protected final Map<Class<?>, NamedCompletionHandler> typeCompletionHandlers = new ConcurrentHashMap<>();
    protected List<CommandMeta> registeredCommands = new ArrayList<>();

    protected ErrorHandler errorHandler = new DefaultErrorHandler();
    protected ResultHandler resultHandler = new DefaultResultHandler();
    protected TextHandler textHandler = new DefaultTextHandler();
    protected MissingArgumentHandler missingArgumentHandler = new DefaultMissingArgumentHandler();
    protected AccessHandler accessHandler = new DefaultAccessHandler();
    protected CompletionHandler completionHandler = new DefaultCompletionHandler();
    protected InstanceCreatorHandler instanceCreatorHandler = new DefaultInstanceCreatorHandler();
    protected ParameterValidationHandler parameterValidationHandler = new DefaultParameterValidationHandler();

    public OkaeriCommands() {
        this.registerType(new DefaultCommandsTypes());
    }

    @Override
    public OkaeriCommands errorHandler(@NonNull ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    public OkaeriCommands resultHandler(@NonNull ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        return this;
    }

    @Override
    public OkaeriCommands textHandler(@NonNull TextHandler textHandler) {
        this.textHandler = textHandler;
        return this;
    }

    @Override
    public OkaeriCommands missingArgumentHandler(@NonNull MissingArgumentHandler argumentHandler) {
        this.missingArgumentHandler = argumentHandler;
        return this;
    }

    @Override
    public OkaeriCommands accessHandler(@NonNull AccessHandler accessHandler) {
        this.accessHandler = accessHandler;
        return this;
    }

    @Override
    public OkaeriCommands completionHandler(@NonNull CompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
        this.completionHandler.registerNamed(this);
        return this;
    }

    @Override
    public OkaeriCommands instanceCreatorHandler(@NonNull InstanceCreatorHandler creatorHandler) {
        this.instanceCreatorHandler = creatorHandler;
        return this;
    }

    @Override
    public OkaeriCommands parameterValidationHandler(@NonNull ParameterValidationHandler validationHandler) {
        this.parameterValidationHandler = validationHandler;
        return this;
    }

    @Override
    public Commands registerCommand(@NonNull Class<? extends CommandService> clazz) {
        return this.registerCommand(this.getInstanceCreatorHandler().createInstance(clazz));
    }

    @Override
    public Commands registerCommand(@NonNull CommandService service) {
        return this.registerCommand(null, service);
    }

    protected Commands registerCommand(ServiceMeta parent, @NonNull CommandService service) {

        Class<? extends CommandService> clazz = service.getClass();
        for (Method method : clazz.getDeclaredMethods()) {

            Executor executor = method.getAnnotation(Executor.class);
            if (executor == null) {
                continue;
            }

            ServiceMeta serviceMeta = ServiceMeta.of(this, parent, service);
            List<CommandMeta> commands = CommandMeta.of(this, service, serviceMeta, method);

            for (CommandMeta command : commands) {
                // standard register
                this.registeredCommands.add(command);
                // cached resolve
                Stream.concat(Stream.of(command.getService().getLabel()), command.getService().getAliases().stream()).forEach(label -> {
                    List<CommandMeta> currentServices = this.registeredCommandsByLabel.computeIfAbsent(label, cLabel -> new ArrayList<>());
                    currentServices.add(command);
                    this.registeredCommandsByLabel.put(label, currentServices);
                    currentServices.sort(META_COMPARATOR);
                });
                // callback
                this.onRegister(command);
            }

            for (Class<? extends CommandService> nestedServiceType : serviceMeta.getNested()) {
                this.registerCommand(serviceMeta, this.getInstanceCreatorHandler().createInstance(nestedServiceType));
            }
        }

        this.registeredCommands.sort(META_COMPARATOR);
        return this;
    }

    @Override
    public Commands registerType(@NonNull TypeResolver typeResolver) {
        this.typeResolvers.add(0, typeResolver);
        this.resolverCache.clear();
        return this;
    }

    @Override
    public <T> Commands registerType(@NonNull Class<T> type, @NonNull Function<String, T> consumer) {
        return this.registerType(type, (inv, com, arg, text) -> consumer.apply(text));
    }

    @Override
    public <T> Commands registerType(@NonNull Class<T> type, @NonNull SimpleTypeResolverAdapter<T> adapter) {
        return this.registerType(new SimpleTypeResolver<T>(type, adapter));
    }

    @Override
    public Commands registerTypeExclusive(@NonNull Type removeAnyForType, @NonNull TypeResolver typeResolver) {
        this.typeResolvers.removeIf(resolver -> resolver.supports(removeAnyForType));
        this.typeResolvers.add(0, typeResolver);
        this.resolverCache.clear();
        return this;
    }

    @Override
    public Commands registerType(@NonNull CommandsExtension typesPack) {
        typesPack.register(this);
        this.resolverCache.clear();
        return this;
    }

    @Override
    public Commands registerExtension(@NonNull CommandsExtension extension) {
        extension.register(this);
        return this;
    }

    @Override
    public Commands registerCompletion(@NonNull String name, @NonNull NamedCompletionHandler handler, boolean auto) {

        if (auto) {
            this.namedCompletionHandlers.put(name, (completionData, argument, invocation, data) -> CompletionHandler.filter(
                CompletionHandler.getLimit(argument, invocation),
                CompletionHandler.stringFilter(invocation),
                handler.complete(completionData, argument, invocation, data).stream()
            ));
            return this;
        }

        this.namedCompletionHandlers.put(name, handler);
        return this;
    }

    @Override
    public Commands registerCompletion(@NonNull String name, @NonNull Supplier<Stream<String>> streamHandler) {
        this.namedCompletionHandlers.put(name, (completionData, argument, invocation, data) -> CompletionHandler.filter(
            CompletionHandler.getLimit(argument, invocation),
            CompletionHandler.stringFilter(invocation),
            streamHandler.get()
        ));
        return this;
    }

    @Override
    public Commands registerCompletion(@NonNull String name, @NonNull Function<CommandData, Stream<String>> streamHandler) {
        this.namedCompletionHandlers.put(name, (completionData, argument, invocation, data) -> CompletionHandler.filter(
            CompletionHandler.getLimit(argument, invocation),
            CompletionHandler.stringFilter(invocation),
            streamHandler.apply(data)
        ));
        return this;
    }

    @Override
    public Commands registerCompletion(@NonNull Class<?> type, @NonNull NamedCompletionHandler handler, boolean auto) {

        if (auto) {
            this.typeCompletionHandlers.put(type, (completionData, argument, invocation, data) -> CompletionHandler.filter(
                CompletionHandler.getLimit(argument, invocation),
                CompletionHandler.stringFilter(invocation),
                handler.complete(completionData, argument, invocation, data).stream()
            ));
            return this;
        }

        this.typeCompletionHandlers.put(type, handler);
        return this;
    }

    @Override
    public Commands registerCompletion(@NonNull Class<?> type, @NonNull Supplier<Stream<String>> streamHandler) {
        this.typeCompletionHandlers.put(type, (completionData, argument, invocation, data) -> CompletionHandler.filter(
            CompletionHandler.getLimit(argument, invocation),
            CompletionHandler.stringFilter(invocation),
            streamHandler.get()
        ));
        return this;
    }

    @Override
    public Commands registerCompletion(@NonNull Class<?> type, @NonNull Function<CommandData, Stream<String>> streamHandler) {
        this.typeCompletionHandlers.put(type, (completionData, argument, invocation, data) -> CompletionHandler.filter(
            CompletionHandler.getLimit(argument, invocation),
            CompletionHandler.stringFilter(invocation),
            streamHandler.apply(data)
        ));
        return this;
    }

    @Override
    public String resolveText(@NonNull String text) {
        return this.getTextHandler().resolve(text);
    }

    @Override
    public String resolveText(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull String text) {
        return this.getTextHandler().resolve(data, invocation, text);
    }

    @Override
    public Object resolveMissingArgument(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {
        return this.getMissingArgumentHandler().resolve(invocation, data, command, param, index);
    }

    @Override
    public List<CommandMeta> findByLabel(@NonNull String label) {
        return this.registeredCommandsByLabel.getOrDefault(label, Collections.emptyList());
    }

    @Override
    public Optional<CommandMeta> findByLabelAndArgs(@NonNull String label, @NonNull String args) {
        return this.findByLabel(label).stream()
            .filter(candidate -> candidate.getExecutor().getPattern().matches(args))
            .findFirst();
    }

    @Override
    public Optional<TypeResolver> findTypeResolver(@NonNull Type type) {

        if (this.resolverCache.containsKey(type)) {
            return Optional.of(this.resolverCache.get(type));
        }

        return this.typeResolvers.stream()
            .filter(resolver -> resolver.supports(type))
            .findFirst()
            .map(resolver -> {
                this.resolverCache.put(type, resolver);
                return resolver;
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(@NonNull String command) throws Exception {

        Optional<Invocation> context = this.invocationMatch(command);
        if (!context.isPresent()) {
            throw new NoSuchCommandException("cannot call '" + command + "', no executor available");
        }

        Invocation invocation = context.get();
        CommandData data = new CommandData();

        InvocationMeta invocationMeta = this.invocationPrepare(invocation, data);
        ServiceMeta service = invocation.getService();

        if (service != null) {
            CommandService implementor = service.getImplementor();
            implementor.preInvoke(invocation, data, invocationMeta);
        }

        return (T) invocationMeta.call();
    }

    @Override
    public List<String> complete(@NonNull List<CommandMeta> metas, @NonNull Invocation invocation, @NonNull CommandData data) {

        if (metas.isEmpty()) {
            return Collections.emptyList();
        }

        String args = invocation.getArgs();
        List<String> completions = new ArrayList<>();

        for (CommandMeta meta : metas) {

            Invocation localInvocation = Invocation.of(
                meta,
                invocation.getLabel(),
                invocation.getArgs()
            );

            ServiceMeta service = meta.getService();
            if (!this.getAccessHandler().allowAccess(service, localInvocation, data, false)) {
                continue;
            }

            ExecutorMeta executor = meta.getExecutor();
            if (!this.getAccessHandler().allowAccess(executor, localInvocation, data)) {
                continue;
            }

            PatternMeta pattern = executor.getPattern();
            Optional<PatternElement> elementOptional = pattern.getCurrentElement(args);
            String lastArgLower = localInvocation.getLastArg().toLowerCase(Locale.ROOT);

            if (!elementOptional.isPresent()) {
                continue;
            }

            // static element, just add
            PatternElement element = elementOptional.get();
            if (element instanceof StaticElement) {
                completions.add(element.getName());
            }
            // process required/optional
            else {
                Optional<ArgumentMeta> argumentOptional = pattern.getArgumentByName(element.getName());
                if (argumentOptional.isPresent()) {

                    // get argument executor completions
                    ArgumentMeta argumentMeta = argumentOptional.get();
                    List<String> executorCompletions = executor.getCompletion().getCompletions(element.getName());

                    // no completions from executor
                    if (executorCompletions.isEmpty()) {
                        // add completions from type completion handler
                        NamedCompletionHandler typeCompletionHandler = this.typeCompletionHandlers.get(argumentMeta.getType());
                        if (typeCompletionHandler != null) {
                            completions.addAll(typeCompletionHandler.complete(executor.getCompletion(), argumentMeta, localInvocation, data));
                        }
                        // add completions from general completion handler
                        else {
                            List<String> generalCompletions = this.getCompletionHandler().complete(argumentMeta, localInvocation, data);
                            completions.addAll(generalCompletions);
                        }
                    }
                    // process completions if applicable
                    else {
                        for (String completion : executorCompletions) {
                            // named completion
                            if (completion.startsWith("@")) {
                                // get name from the special tag
                                String completionName = completion.substring(1);
                                NamedCompletionHandler completionHandler = this.namedCompletionHandlers.get(completionName);
                                // if handled found receive completions and add all
                                if (completionHandler != null) {
                                    completions.addAll(completionHandler.complete(executor.getCompletion(), argumentMeta, localInvocation, data));
                                }
                            }
                            // simple completion, just add if matching
                            else if (localInvocation.isOpenArgs() || completion.toLowerCase(Locale.ROOT).startsWith(lastArgLower)) {
                                completions.add(completion);
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(new TreeSet<>(completions));
    }

    @Override
    public List<String> complete(@NonNull String command) {
        String[] parts = command.split(" ", 2);
        String label = parts[0];
        String args = (parts.length > 1) ? parts[1] : "";
        List<CommandMeta> metas = this.findByLabel(label);
        return this.complete(metas, Invocation.of(label, args), new CommandData());
    }

    @Override
    public Optional<Invocation> invocationMatch(@NonNull String command) {

        String[] parts = command.split(" ", 2);
        String label = parts[0];
        String args = (parts.length > 1) ? parts[1] : "";

        Optional<CommandMeta> commandMetas = this.findByLabelAndArgs(label, args);
        if (!commandMetas.isPresent()) {
            return Optional.empty();
        }

        CommandMeta commandMeta = commandMetas.get();
        return Optional.of(Invocation.of(commandMeta, label, args));
    }

    @Override
    public InvocationMeta invocationPrepare(@NonNull Invocation invocation, @NonNull CommandData data) {

        String args = invocation.getArgs();
        CommandMeta commandMeta = invocation.getCommand();
        if (commandMeta == null) {
            throw new IllegalArgumentException("Cannot use dummy context for prepare: " + invocation);
        }

        ExecutorMeta executor = commandMeta.getExecutor();
        PatternMeta pattern = executor.getPattern();
        List<ArgumentMeta> arguments = executor.getArguments();

        Map<Integer, Object> cmdIndexToObjectMap = new LinkedHashMap<>();
        Map<ArgumentMeta, Object> metaToObjectMap = new LinkedHashMap<>();
        Map<Integer, ArgumentMeta> cmdIndexToMetaMap = new LinkedHashMap<>();
        String[] argsArr = args.split(" ");

        if ((argsArr.length == 1) && argsArr[0].isEmpty()) {
            argsArr = new String[0];
        }

        // call pre-resolve
        CommandService implementor = commandMeta.getService().getImplementor();
        implementor.preResolve(invocation, data);

        // resolve command text arguments to value mappings
        for (ArgumentMeta argument : arguments) {

            String value = pattern.getValueByArgument(argument, argsArr);
            Optional<TypeResolver> typeResolverOptional = this.findTypeResolver(argument.getParameterizedType());

            if (!typeResolverOptional.isPresent()) {
                throw new IllegalArgumentException("method argument of type " + argument.getType() + " cannot be resolved");
            }

            Object resolvedValue;
            try {
                if (!argument.isOptional() && (value == null)) {
                    throw new IllegalArgumentException("non-optional argument was null");
                } else if (argument.isOptional() && (value == null)) {
                    if (argument.getDefaultValue().isEmpty() || Arg.NULL.equals(argument.getDefaultValue())) {
                        resolvedValue = null;
                    } else {
                        resolvedValue = typeResolverOptional.get().resolve(invocation, data, argument, argument.getDefaultValue());
                        if (resolvedValue == null) {
                            throw new IllegalArgumentException("cannot resolve argument from default value");
                        }
                    }
                } else {
                    resolvedValue = typeResolverOptional.get().resolve(invocation, data, argument, value);
                    if (!argument.isOptional() && (resolvedValue == null)) {
                        throw new IllegalArgumentException("cannot resolve argument");
                    }
                }
            } catch (Exception exception) {
                throw new CommandException(argument.getName() + " - " + exception.getMessage(), exception);
            }

            cmdIndexToObjectMap.put(argument.getIndex(), argument.wrap(resolvedValue));
            cmdIndexToMetaMap.put(argument.getIndex(), argument);
        }

        if (arguments.size() != cmdIndexToObjectMap.size()) {
            throw new IllegalArgumentException("method arguments size (" + arguments.size() + ") does not match call arguments size (" + cmdIndexToObjectMap.size() + ")");
        }

        Method executorMethod = executor.getMethod();
        Parameter[] methodParameters = executorMethod.getParameters();
        int parametersLength = methodParameters.length;
        Object[] call = new Object[parametersLength];

        // fill method call with command arguments and others
        for (int i = 0; i < parametersLength; i++) {

            // argument present
            Object callArgument = cmdIndexToObjectMap.get(i);
            if (callArgument != null) {
                call[i] = callArgument;
                continue;
            }

            // check for RawArgs
            Parameter param = methodParameters[i];
            Class<?> paramType = param.getType();
            if (param.getAnnotation(RawArgs.class) != null) {
                if (CharSequence.class.isAssignableFrom(paramType)) {
                    call[i] = args;
                    continue;
                }
                if (List.class.isAssignableFrom(paramType)) {
                    call[i] = args.isEmpty() ? Collections.emptyList() : Arrays.asList(argsArr);
                    continue;
                }
                if (paramType.isArray() && CharSequence.class.isAssignableFrom(paramType.getComponentType())) {
                    call[i] = args.isEmpty() ? new String[0] : argsArr;
                    continue;
                }
                throw new IllegalArgumentException("@RawArgs type cannot be " + paramType + " [allowed: String, List<String>, String[]]");
            }

            // check for label
            if (param.getAnnotation(Label.class) != null) {
                if (CharSequence.class.isAssignableFrom(paramType)) {
                    call[i] = invocation.getLabel();
                    continue;
                }
                throw new IllegalArgumentException("@Label type cannot be " + paramType + " [allowed: String]");
            }

            // pass to adapter for missing elements
            call[i] = this.resolveMissingArgument(invocation, data, commandMeta, param, i);
        }

        // validate
        for (int i = 0; i < call.length; i++) {

            Parameter param = methodParameters[i];
            Object value = call[i];

            ValidationResult validationResult = this.parameterValidationHandler.validate(invocation, data, commandMeta, param, value, i);
            if (validationResult.isValid()) {
                continue;
            }

            ArgumentMeta argument = cmdIndexToMetaMap.get(i);
            throw new CommandException(argument.getName() + " - " + validationResult.getMessage());
        }

        // check late access
        InvocationMeta invocationMeta = InvocationMeta.of(invocation, data, executor, call);
        this.accessHandler.checkCall(invocationMeta);

        // read to go!
        return invocationMeta;
    }

    @Override
    public void onRegister(@NonNull CommandMeta command) {
    }

    @Override
    public void close() {
    }
}
