package eu.okaeri.commands.brigadier.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is a 'temporary' measure to disable
 * brigadier in some commands and not the others while
 * various implementation issues remain unaddressed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BrigadierDisabled {
}
