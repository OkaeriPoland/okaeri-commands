package eu.okaeri.commands.annotation;

import eu.okaeri.commands.completion.CompletionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completion {

    String[] arg();
    String[] value();
    Class<? extends CompletionProvider> provider() default DEFAULT.class;
    CompletionMeta[] meta() default {};

    abstract class DEFAULT implements CompletionProvider {
    }
}
