package eu.okaeri.commands.validator;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.validator.Validator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandsValidator implements CommandsExtension {

    private final Validator validator;

    @Override
    public void register(Commands commands) {
        commands.parameterValidationHandler(new OkaeriParameterValidationHandler(this.validator));
    }
}
