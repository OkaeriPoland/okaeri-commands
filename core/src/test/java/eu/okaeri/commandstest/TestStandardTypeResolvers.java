package eu.okaeri.commandstest;

import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.commands.type.resolver.BooleanTypeResolver;
import eu.okaeri.commands.type.resolver.ByteTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestStandardTypeResolvers {

    private static final InvocationContext DUMMY_IC = InvocationContext.of("dummy", "");
    private static final CommandContext DUMMY_CC = new CommandContext();
    private static final ArgumentMeta DUMMY_AM = new ArgumentMeta();

    @Test
    public void test_boolean_types() {

        BooleanTypeResolver resolver = new BooleanTypeResolver();
        assertTrue(resolver.supports(boolean.class));
        assertTrue(resolver.supports(Boolean.class));

        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "true"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "y"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "yes"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "on"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "1"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "True"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "TRUE"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "Yes"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "YES"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "On"));
        assertTrue(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "ON"));

        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "false"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "n"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "no"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "off"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "0"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "False"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "FALSE"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "No"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "NO"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "Off"));
        assertFalse(resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "OFF"));

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "hi"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "01"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "i like tests"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "nó, really"));
    }

    @Test
    public void test_byte_types() {

        ByteTypeResolver resolver = new ByteTypeResolver();
        assertTrue(resolver.supports(byte.class));
        assertTrue(resolver.supports(Byte.class));

        assertEquals(Byte.MIN_VALUE, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, String.valueOf(Byte.MIN_VALUE)));
        assertEquals((byte) -1, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "-1"));
        assertEquals((byte) -1, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "-1.0"));
        assertEquals((byte) 0, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "0"));
        assertEquals((byte) 0, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "0.0"));
        assertEquals((byte) 1, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "1"));
        assertEquals((byte) 1, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "1.0"));
        assertEquals((byte) 2, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "2"));
        assertEquals((byte) 2, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "2.0"));
        assertEquals(Byte.MAX_VALUE, resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, String.valueOf(Byte.MAX_VALUE)));

        assertThrows(ArithmeticException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "1.23"));
        assertThrows(ArithmeticException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, "-2.01"));

        assertThrows(ArithmeticException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, String.valueOf(((long) Byte.MIN_VALUE) - 1)));
        assertThrows(ArithmeticException.class, () -> resolver.resolve(DUMMY_IC, DUMMY_CC, DUMMY_AM, String.valueOf(((long) Byte.MAX_VALUE) + 1)));
    }
}
