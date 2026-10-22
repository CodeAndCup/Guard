package fr.perrier.guard.commands.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    String name();

    boolean wildcard() default (false);

    String baseValue() default ("");

    String[] tabCompleteFlags() default ("");
}
