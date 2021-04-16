package eu.okaeri.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Executor {
    boolean async() default false;
    String[] pattern() default {};
    String description() default "";
    String usage() default "{label} {pattern}";
}
