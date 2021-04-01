package eu.okaeri.commands.meta;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.service.Option;
import lombok.*;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Data
public class ArgumentMeta {

    public static boolean isArg(Parameter parameter) {
        return (parameter.getAnnotation(Arg.class) != null) && (parameter.getAnnotation(RawArgs.class) != null);
    }

    @SneakyThrows
    public static ArgumentMeta of(Parameter parameter) {

        Arg arg = parameter.getAnnotation(Arg.class);
        RawArgs rawArgs = parameter.getAnnotation(RawArgs.class);

        if ((arg == null) && (rawArgs == null)) {
            throw new IllegalArgumentException("cannot create ArgumentMeta from Parameter without @Arg/@RawArgs annotation");
        }

        Class<? extends Parameter> parameterClass = parameter.getClass();
        Field indexField = parameterClass.getDeclaredField("index");
        boolean accessible = indexField.isAccessible();
        indexField.setAccessible(true);
        int indexValue = (int) indexField.get(parameter);
        indexField.setAccessible(accessible);

        ArgumentMeta meta = new ArgumentMeta();
        meta.name = (arg == null) ? null : arg.value();
        meta.rawArgs = rawArgs != null;
        meta.index = indexValue;
        meta.type = parameter.getType();
        meta.optional = Option.class.isAssignableFrom(meta.type) || Optional.class.isAssignableFrom(meta.type);

        return meta;
    }

    private boolean rawArgs;
    private boolean optional;
    private String name;
    private int index;
    private Class<?> type;

    public Object wrap(Object value) {
        if (Option.class.isAssignableFrom(this.type)) {
            return Option.of(value);
        } else if (Optional.class.isAssignableFrom(this.type)) {
            return Optional.of(value);
        } else {
            return value;
        }
    }
}
