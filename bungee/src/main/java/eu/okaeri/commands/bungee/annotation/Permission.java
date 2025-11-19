package eu.okaeri.commands.bungee.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies permission requirements for commands and executors.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    /**
     * Required permission node(s). Supports placeholder resolution through TextHandler.
     *
     * <p>Examples:</p>
     * <pre>{@code
     * @Permission("myplugin.admin")
     * @Permission({"perm1", "perm2"})  // With mode=ANY, needs at least one
     * @Permission(value = "plugin.{arg0}", mode = Mode.ALL)  // Dynamic permission with placeholder
     * }</pre>
     *
     * @return the permission node(s) required
     */
    String[] value();

    /**
     * Permission check mode.
     * <ul>
     *   <li>{@link Mode#ANY}: Sender needs at least one of the specified permissions (default)</li>
     *   <li>{@link Mode#ALL}: Sender needs all specified permissions</li>
     * </ul>
     *
     * @return the permission check mode
     */
    Mode mode() default Mode.ANY;

    /**
     * Custom error message shown when permission check fails.
     * Supports multiple formats:
     * <ul>
     *   <li><b>Empty string</b> (default): Uses built-in error message showing required permission(s)</li>
     *   <li><b>i18n key</b>: Format: {@code ${key}} - Resolved through TextHandler (e.g., {@code ${noPermissionError}})</li>
     *   <li><b>Plain text</b>: Direct message (e.g., {@code "You don't have permission!"})</li>
     * </ul>
     *
     * <p>Examples:</p>
     * <pre>{@code
     * // Default error (shows permission node)
     * @Permission("myplugin.admin")
     *
     * // i18n key (resolved through TextHandler)
     * @Permission(value = "myplugin.admin", deny = "${noAdminPermission}")
     *
     * // Plain text message
     * @Permission(value = "myplugin.admin", deny = "You need admin permission!")
     *
     * // With placeholders
     * @Permission(value = "plugin.{arg0}", deny = "${needPermissionFor}")
     * }</pre>
     *
     * <p>Global overrides available in your i18n system:</p>
     * <ul>
     *   <li>{@code ${commandSystemPermissionsError}} - Default error showing permission node (with {permission} placeholder)</li>
     *   <li>{@code ${commandSystemAccessMessageError}} - Wrapper for custom plain text messages (with {message} placeholder)</li>
     * </ul>
     *
     * @return the custom error message, or empty string to use the default
     */
    String deny() default "";

    /**
     * Permission check mode.
     */
    enum Mode {
        /**
         * Requires at least one of the specified permissions.
         */
        ANY,
        /**
         * Requires all specified permissions.
         */
        ALL
    }
}
