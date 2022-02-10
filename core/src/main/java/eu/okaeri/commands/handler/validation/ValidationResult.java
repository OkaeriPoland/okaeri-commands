package eu.okaeri.commands.handler.validation;

import lombok.Data;

@Data(staticConstructor = "of")
public class ValidationResult {

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, message);
    }

    public static ValidationResult ok(String message) {
        return new ValidationResult(true, message);
    }

    private final boolean valid;
    private final String message;
}
