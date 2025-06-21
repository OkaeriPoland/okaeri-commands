package eu.okaeri.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {

    /**
     * Use @Arg(or = NULL) to allow wrapperless nullable optional args
     */
    String NULL = "$$__null__$$";

    /**
     * @return Argument name (leave empty to use param name)
     */
    String value() default "";

    /**
     * @return Fallback for optional arguments
     */
    String or() default "";
}
