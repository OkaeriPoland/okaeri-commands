package eu.okaeri.commands.bukkit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    String[] value();

    Mode mode() default Mode.ANY;

    enum Mode {
        ANY,
        ALL
    }
}
