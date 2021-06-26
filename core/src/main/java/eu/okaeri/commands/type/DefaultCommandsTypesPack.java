package eu.okaeri.commands.type;

import eu.okaeri.commands.type.resolver.*;

public class DefaultCommandsTypesPack implements CommandsTypesPack {

    @Override
    public void register(CommandsTypes types) {
        types.register(new ByteTypeResolver());
        types.register(new ShortTypeResolver());
        types.register(new CharacterTypeResolver());
        types.register(new FloatTypeResolver());
        types.register(new LongTypeResolver());
        types.register(new EnumTypeResolver());
        types.register(new BooleanTypeResolver());
        types.register(new DoubleTypeResolver());
        types.register(new IntegerTypeResolver());
        types.register(new CharSequenceTypeResolver());
    }
}
