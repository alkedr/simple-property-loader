package com.github.alkedr.tests;

import com.github.alkedr.SimplePropertyLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.github.alkedr.SimplePropertyLoader.getOptionalSystemProperty;
import static com.github.alkedr.SimplePropertyLoader.getSystemProperty;
import static org.junit.Assert.*;

public class SimplePropertyLoaderTest {
    private static final Map<String, String> properties = new HashMap<>();
    private static final SimplePropertyLoader spl = new SimplePropertyLoader(properties::get);

    @BeforeClass
    public static void setUp() {
        initializeProperties(System::setProperty);
        initializeProperties(properties::put);
    }

    private static void initializeProperties(BiConsumer<String, String> setProperty) {
        setProperty.accept("number", "1");
        setProperty.accept("notANumber", "q");
        setProperty.accept("numberList", "1,2,3");
        setProperty.accept("numberListWithNotANumber", "1,q,3");
    }


    @Test
    public void propertyIsNotDefinedException() {
        assertEquals("Property '123' is not defined", new SimplePropertyLoader.PropertyIsNotDefinedException("123").getMessage());
    }

    @Test
    public void propertyValueParsingException() {
        RuntimeException cause = new RuntimeException("345");
        SimplePropertyLoader.PropertyValueParsingException e = new SimplePropertyLoader.PropertyValueParsingException("123", "234", cause);
        assertEquals("Can't parse value of property '123': '234': 345", e.getMessage());
        assertSame(cause, e.getCause());
    }



    @Test
    public void getProperty_string_present() {
        assertEquals("1", spl.getProperty("number"));
    }

    @Test
    public void getProperty_string_absent() {
        try {
            spl.getProperty("absent");
            fail();
        } catch (SimplePropertyLoader.PropertyIsNotDefinedException e) {
            assertTrue(e.getMessage().contains("'absent'"));
            assertNull(e.getCause());
        }
    }


    @Test
    public void getProperty_withParser_present() {
        assertEquals(Integer.valueOf(1), spl.getProperty("number", Integer::valueOf));
    }

    @Test
    public void getProperty_withParser_absent() {
        try {
            spl.getProperty("absent", Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyIsNotDefinedException e) {
            assertTrue(e.getMessage().contains("'absent'"));
            assertNull(e.getCause());
        }
    }

    @Test
    public void getProperty_withParser_parserThrows() {
        try {
            spl.getProperty("notANumber", Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'notANumber'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }


    @Test
    public void getProperty_withSplitterAndParser_present() {
        assertArrayEquals(
                new Integer[]{1, 2, 3},
                spl.getProperty("numberList", Pattern.compile(",")::splitAsStream, Integer::valueOf).toArray(Integer[]::new)
        );
    }

    @Test
    public void getProperty_withSplitterAndParser_absent() {
        try {
            spl.getProperty("absent", Pattern.compile(",")::splitAsStream, Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyIsNotDefinedException e) {
            assertTrue(e.getMessage().contains("'absent'"));
            assertNull(e.getCause());
        }
    }

    @Test
    public void getProperty_withSplitterAndParser_splitterThrows() {
        try {
            spl.getProperty("numberList", s -> {throw new RuntimeException();}, Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberList'"));
            assertSame(RuntimeException.class, e.getCause().getClass());
        }
    }

    @Test
    public void getProperty_withSplitterAndParser_parserThrows() {
        try {
            spl.getProperty("numberListWithNotANumber", Pattern.compile(",")::splitAsStream, Integer::valueOf).toArray(Integer[]::new);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberListWithNotANumber[1]'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }



    @Test
    public void getOptionalProperty_string_present() {
        assertEquals(Optional.of("1"), spl.getOptionalProperty("number"));
    }

    @Test
    public void getOptionalProperty_string_absent() {
        assertEquals(Optional.empty(), spl.getOptionalProperty("absent"));
    }


    @Test
    public void getOptionalProperty_withParser_present() {
        assertEquals(Optional.of(1), spl.getOptionalProperty("number", Integer::valueOf));
    }

    @Test
    public void getOptionalProperty_withParser_absent() {
        assertEquals(Optional.empty(), spl.getOptionalProperty("absent", Integer::valueOf));
    }

    @Test
    public void getOptionalProperty_withParser_parserThrows() {
        try {
            spl.getOptionalProperty("notANumber", Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'notANumber'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }


    @Test
    public void getOptionalProperty_withSplitterAndParser_present() {
        assertArrayEquals(
                new Integer[]{1, 2, 3},
                spl.getOptionalProperty("numberList", Pattern.compile(",")::splitAsStream, Integer::valueOf)
                        .orElseThrow(() -> new AssertionError("expected not empty"))
                        .toArray(Integer[]::new)
        );
    }

    @Test
    public void getOptionalProperty_withSplitterAndParser_absent() {
        assertEquals(Optional.empty(), spl.getOptionalProperty("absent", Pattern.compile(",")::splitAsStream, Integer::valueOf));
    }

    @Test
    public void getOptionalProperty_withSplitterAndParser_splitterThrows() {
        try {
            spl.getOptionalProperty("numberList", s -> {throw new RuntimeException();}, Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberList'"));
            assertSame(RuntimeException.class, e.getCause().getClass());
        }
    }

    @Test
    public void getOptionalProperty_withSplitterAndParser_parserThrows() {
        try {
            spl.getOptionalProperty("numberListWithNotANumber", Pattern.compile(",")::splitAsStream, Integer::valueOf)
                    .orElseThrow(() -> new AssertionError("expected not empty"))
                    .toArray(Integer[]::new);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberListWithNotANumber[1]'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }



    @Test
    public void getSystemProperty_string_present() {
        assertEquals("1", getSystemProperty("number"));
    }

    @Test
    public void getSystemProperty_string_absent() {
        try {
            getSystemProperty("absent");
            fail();
        } catch (SimplePropertyLoader.PropertyIsNotDefinedException e) {
            assertTrue(e.getMessage().contains("'absent'"));
            assertNull(e.getCause());
        }
    }


    @Test
    public void getSystemProperty_withParser_present() {
        assertEquals(Integer.valueOf(1), getSystemProperty("number", Integer::valueOf));
    }

    @Test
    public void getSystemProperty_withParser_absent() {
        try {
            getSystemProperty("absent", Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyIsNotDefinedException e) {
            assertTrue(e.getMessage().contains("'absent'"));
            assertNull(e.getCause());
        }
    }

    @Test
    public void getSystemProperty_withParser_parserThrows() {
        try {
            getSystemProperty("notANumber", Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'notANumber'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }


    @Test
    public void getSystemProperty_withSplitterAndParser_present() {
        assertArrayEquals(
                new Integer[]{1, 2, 3},
                getSystemProperty("numberList", Pattern.compile(",")::splitAsStream, Integer::valueOf).toArray(Integer[]::new)
        );
    }

    @Test
    public void getSystemProperty_withSplitterAndParser_absent() {
        try {
            getSystemProperty("absent", Pattern.compile(",")::splitAsStream, Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyIsNotDefinedException e) {
            assertTrue(e.getMessage().contains("'absent'"));
            assertNull(e.getCause());
        }
    }

    @Test
    public void getSystemProperty_withSplitterAndParser_splitterThrows() {
        try {
            getSystemProperty("numberList", s -> {throw new RuntimeException();}, Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberList'"));
            assertSame(RuntimeException.class, e.getCause().getClass());
        }
    }

    @Test
    public void getSystemProperty_withSplitterAndParser_parserThrows() {
        try {
            getSystemProperty("numberListWithNotANumber", Pattern.compile(",")::splitAsStream, Integer::valueOf).toArray(Integer[]::new);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberListWithNotANumber[1]'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }



    @Test
    public void getOptionalSystemProperty_string_present() {
        assertEquals(Optional.of("1"), getOptionalSystemProperty("number"));
    }

    @Test
    public void getOptionalSystemProperty_string_absent() {
        assertEquals(Optional.empty(), getOptionalSystemProperty("absent"));
    }


    @Test
    public void getOptionalSystemProperty_withParser_present() {
        assertEquals(Optional.of(1), getOptionalSystemProperty("number", Integer::valueOf));
    }

    @Test
    public void getOptionalSystemProperty_withParser_absent() {
        assertEquals(Optional.empty(), getOptionalSystemProperty("absent", Integer::valueOf));
    }

    @Test
    public void getOptionalSystemProperty_withParser_parserThrows() {
        try {
            getOptionalSystemProperty("notANumber", Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'notANumber'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }


    @Test
    public void getOptionalSystemProperty_withSplitterAndParser_present() {
        assertArrayEquals(
                new Integer[]{1, 2, 3},
                getOptionalSystemProperty("numberList", Pattern.compile(",")::splitAsStream, Integer::valueOf)
                        .orElseThrow(() -> new AssertionError("expected not empty"))
                        .toArray(Integer[]::new)
        );
    }

    @Test
    public void getOptionalSystemProperty_withSplitterAndParser_absent() {
        assertEquals(Optional.empty(), getOptionalSystemProperty("absent", Pattern.compile(",")::splitAsStream, Integer::valueOf));
    }

    @Test
    public void getOptionalSystemProperty_withSplitterAndParser_splitterThrows() {
        try {
            getOptionalSystemProperty("numberList", s -> {throw new RuntimeException();}, Integer::valueOf);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberList'"));
            assertSame(RuntimeException.class, e.getCause().getClass());
        }
    }

    @Test
    public void getOptionalSystemProperty_withSplitterAndParser_parserThrows() {
        try {
            getOptionalSystemProperty("numberListWithNotANumber", Pattern.compile(",")::splitAsStream, Integer::valueOf)
                    .orElseThrow(() -> new AssertionError("expected not empty"))
                    .toArray(Integer[]::new);
            fail();
        } catch (SimplePropertyLoader.PropertyValueParsingException e) {
            assertTrue(e.getMessage().contains("'numberListWithNotANumber[1]'"));
            assertSame(NumberFormatException.class, e.getCause().getClass());
        }
    }
}
