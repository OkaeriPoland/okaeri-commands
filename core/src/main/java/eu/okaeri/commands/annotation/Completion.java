package eu.okaeri.commands.annotation;

import eu.okaeri.commands.handler.completion.NamedCompletionHandler;

import java.lang.annotation.*;

@Repeatable(Completions.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completion {

    String[] arg();

    String[] value() default {};

    Class<? extends NamedCompletionHandler> provider() default DEFAULT.class;

    CompletionData[] data() default {};

    abstract class DEFAULT implements NamedCompletionHandler {
    }
}
