package eu.okaeri.commandstest;

import eu.okaeri.commands.type.resolver.BooleanTypeResolver;
import eu.okaeri.commands.type.resolver.ByteTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestStandardTypeResolvers {

    @Test
    public void test_boolean_types() {

        BooleanTypeResolver resolver = new BooleanTypeResolver();
        assertTrue(resolver.supports(boolean.class));
        assertTrue(resolver.supports(Boolean.class));

        assertTrue(resolver.resolve(null, null, null, "true"));
        assertTrue(resolver.resolve(null, null, null, "y"));
        assertTrue(resolver.resolve(null, null, null, "yes"));
        assertTrue(resolver.resolve(null, null, null, "on"));
        assertTrue(resolver.resolve(null, null, null, "1"));
        assertTrue(resolver.resolve(null, null, null, "True"));
        assertTrue(resolver.resolve(null, null, null, "TRUE"));
        assertTrue(resolver.resolve(null, null, null, "Yes"));
        assertTrue(resolver.resolve(null, null, null, "YES"));
        assertTrue(resolver.resolve(null, null, null, "On"));
        assertTrue(resolver.resolve(null, null, null, "ON"));

        assertFalse(resolver.resolve(null, null, null, "false"));
        assertFalse(resolver.resolve(null, null, null, "n"));
        assertFalse(resolver.resolve(null, null, null, "no"));
        assertFalse(resolver.resolve(null, null, null, "off"));
        assertFalse(resolver.resolve(null, null, null, "0"));
        assertFalse(resolver.resolve(null, null, null, "False"));
        assertFalse(resolver.resolve(null, null, null, "FALSE"));
        assertFalse(resolver.resolve(null, null, null, "No"));
        assertFalse(resolver.resolve(null, null, null, "NO"));
        assertFalse(resolver.resolve(null, null, null, "Off"));
        assertFalse(resolver.resolve(null, null, null, "OFF"));

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null, null, null, "hi"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null, null, null, "01"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null, null, null, "i like tests"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null, null, null, "nó, really"));
    }

    @Test
    public void test_byte_types() {

        ByteTypeResolver resolver = new ByteTypeResolver();
        assertTrue(resolver.supports(byte.class));
        assertTrue(resolver.supports(Byte.class));

        assertEquals(Byte.MIN_VALUE, resolver.resolve(null, null, null, String.valueOf(Byte.MIN_VALUE)));
        assertEquals((byte) -1, resolver.resolve(null, null, null, "-1"));
        assertEquals((byte) -1, resolver.resolve(null, null, null, "-1.0"));
        assertEquals((byte) 0, resolver.resolve(null, null, null, "0"));
        assertEquals((byte) 0, resolver.resolve(null, null, null, "0.0"));
        assertEquals((byte) 1, resolver.resolve(null, null, null, "1"));
        assertEquals((byte) 1, resolver.resolve(null, null, null, "1.0"));
        assertEquals((byte) 2, resolver.resolve(null, null, null, "2"));
        assertEquals((byte) 2, resolver.resolve(null, null, null, "2.0"));
        assertEquals(Byte.MAX_VALUE, resolver.resolve(null, null, null, String.valueOf(Byte.MAX_VALUE)));

        assertThrows(ArithmeticException.class, () -> resolver.resolve(null, null, null, "1.23"));
        assertThrows(ArithmeticException.class, () -> resolver.resolve(null, null, null, "-2.01"));

        assertThrows(ArithmeticException.class, () -> resolver.resolve(null, null, null, String.valueOf(((long) Byte.MIN_VALUE) - 1)));
        assertThrows(ArithmeticException.class, () -> resolver.resolve(null, null, null, String.valueOf(((long) Byte.MAX_VALUE) + 1)));
    }
}
