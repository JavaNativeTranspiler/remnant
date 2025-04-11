/**
 * Created: 2/3/2025
 */
package com.chorus.api.command.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandException extends RuntimeException {
    private final String message;
    private final Throwable cause;

    public CommandException(String message) {
        this(message, null);
    }
}