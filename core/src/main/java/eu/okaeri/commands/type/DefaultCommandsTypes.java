package eu.okaeri.commands.type;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.commands.type.resolver.*;

public class DefaultCommandsTypes implements CommandsExtension {

    @Override
    public void register(Commands commands) {
        commands.registerType(new ByteTypeResolver());
        commands.registerType(new ShortTypeResolver());
        commands.registerType(new CharacterTypeResolver());
        commands.registerType(new FloatTypeResolver());
        commands.registerType(new LongTypeResolver());
        commands.registerType(new EnumTypeResolver());
        commands.registerType(new BooleanTypeResolver());
        commands.registerType(new DoubleTypeResolver());
        commands.registerType(new IntegerTypeResolver());
        commands.registerType(new CharSequenceTypeResolver());
        commands.registerType(new DurationTypeResolver());
    }
}
