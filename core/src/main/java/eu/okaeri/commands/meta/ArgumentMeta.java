package eu.okaeri.commands.meta;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.service.Option;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Optional;

@Data
public class ArgumentMeta {

    public static boolean isArg(Parameter parameter) {
        return parameter.getAnnotation(Arg.class) != null;
    }

    @SneakyThrows
    public static ArgumentMeta of(@NonNull Commands commands, @NonNull Parameter parameter, int index) {

        Arg arg = parameter.getAnnotation(Arg.class);

        if (arg == null) {
            throw new IllegalArgumentException("cannot create ArgumentMeta from Parameter without @Arg annotation");
        }

        ArgumentMeta meta = new ArgumentMeta();
        meta.name = commands.resolveText(arg.value().isEmpty() ? parameter.getName() : arg.value());
        meta.index = index;
        meta.type = parameter.getType();
        meta.parameterizedType = parameter.getParameterizedType();
        meta.optional = Option.class.isAssignableFrom(meta.type) || Optional.class.isAssignableFrom(meta.type);

        return meta;
    }

    private boolean optional;
    private String name;
    private int index;
    private Class<?> type;
    private Type parameterizedType;

    public Object wrap(Object value) {

        if (!this.optional) {
            return value;
        }

        if (Option.class.isAssignableFrom(this.type)) {
            return Option.of(value);
        }

        if (Optional.class.isAssignableFrom(this.type)) {
            return Optional.of(value);
        }

        return value;
    }
}
