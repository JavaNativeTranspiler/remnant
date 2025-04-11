
/**
 * Created: 12/7/2024
 */
package com.chorus.api.module.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModuleException extends RuntimeException {
    private final String message;
    private final Throwable cause;

    public ModuleException(String message) {
        this(message, null);
    }
}