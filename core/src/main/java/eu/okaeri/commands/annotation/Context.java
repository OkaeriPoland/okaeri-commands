package eu.okaeri.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter as a context value to be resolved from CommandData.
 * Commonly used for sender-type parameters (Player, ConsoleCommandSender, etc.).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Context {

    /**
     * Custom error message shown when the sender type doesn't match the expected type.
     * Supports multiple formats:
     * <ul>
     *   <li><b>Empty string</b> (default): Uses built-in error message "This command can only be executed by {type}!"</li>
     *   <li><b>i18n key</b>: Format: {@code ${key}} - Resolved through TextHandler (e.g., {@code ${playerOnlyCommand}})</li>
     *   <li><b>Plain text</b>: Direct message (e.g., {@code "Only players can use this command!"})</li>
     * </ul>
     *
     * <p>Examples:</p>
     * <pre>{@code
     * // Default error message
     * public void command(@Context Player player) { }
     *
     * // i18n key (resolved through TextHandler)
     * public void command(@Context(invalid = "${playerOnlyCommand}") Player player) { }
     *
     * // Plain text message
     * public void command(@Context(invalid = "Only players can use this!") Player player) { }
     * }</pre>
     *
     * <p>Global overrides available in your i18n system:</p>
     * <ul>
     *   <li>{@code ${commandSystemContextError}} - Default error message template (with {expected} placeholder)</li>
     *   <li>{@code ${commandSystemContextMessageError}} - Wrapper for custom plain text messages (with {message} placeholder)</li>
     * </ul>
     *
     * @return the custom error message, or empty string to use the default
     */
    String invalid() default "";
}
