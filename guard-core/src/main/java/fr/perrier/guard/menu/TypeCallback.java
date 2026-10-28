package fr.perrier.guard.menu;

import java.io.Serializable;

public interface TypeCallback<T> extends Serializable {
    void callback(final T p0);
}
