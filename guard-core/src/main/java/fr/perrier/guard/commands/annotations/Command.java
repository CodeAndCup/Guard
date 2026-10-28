package fr.perrier.guard.commands.annotations;

import fr.perrier.guard.utils.commands.CommandsUtils;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    CommandsUtils names();

    String perm() default "";

    boolean async() default false;
}
