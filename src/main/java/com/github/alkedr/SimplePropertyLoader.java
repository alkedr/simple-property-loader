package com.github.alkedr;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class SimplePropertyLoader {
    private static final SimplePropertyLoader PROPERTY_LOADER_FROM_SYSTEM_GET_PROPERTY = new SimplePropertyLoader(System::getProperty);

    private final Function<String, String> propertyNameToNullablePropertyValueFunction;


    public SimplePropertyLoader(Function<String, String> propertyNameToNullablePropertyValueFunction) {
        this.propertyNameToNullablePropertyValueFunction = propertyNameToNullablePropertyValueFunction;
    }


    public String getProperty(String name) {
        return optionalToRequired(name, getOptionalProperty(name));
    }

    public <T> T getProperty(String name, PropertyValueParser<T> parser) {
        return optionalToRequired(name, getOptionalProperty(name, parser));
    }

    public <T> Stream<T> getProperty(String name, PropertyValueParser<Stream<String>> splitter,
                                     PropertyValueParser<T> elementParser) {
        return optionalToRequired(name, getOptionalProperty(name, splitter, elementParser));
    }


    public Optional<String> getOptionalProperty(String name) {
        return Optional.ofNullable(propertyNameToNullablePropertyValueFunction.apply(name));
    }

    public <T> Optional<T> getOptionalProperty(String name, PropertyValueParser<T> parser) {
        return getOptionalProperty(name).map(value -> invokeParser(parser, name, value));
    }

    public <T> Optional<Stream<T>> getOptionalProperty(String name, PropertyValueParser<Stream<String>> splitter,
                                                       PropertyValueParser<T> elementParser) {
        return getOptionalProperty(name)
                .map(value -> {
                    int[] counter = {0};
                    return invokeParser(splitter, name, value)
                            .map(elementValue -> invokeParser(
                                    elementParser,
                                    String.format("%s[%d]", name, counter[0]++),
                                    elementValue
                            ));
                });
    }


    public static String getSystemProperty(String name) {
        return PROPERTY_LOADER_FROM_SYSTEM_GET_PROPERTY.getProperty(name);
    }

    public static <T> T getSystemProperty(String name, PropertyValueParser<T> parser) {
        return PROPERTY_LOADER_FROM_SYSTEM_GET_PROPERTY.getProperty(name, parser);
    }

    public static <T> Stream<T> getSystemProperty(String name, PropertyValueParser<Stream<String>> splitter,
                                                  PropertyValueParser<T> elementParser) {
        return PROPERTY_LOADER_FROM_SYSTEM_GET_PROPERTY.getProperty(name, splitter, elementParser);
    }


    public static Optional<String> getOptionalSystemProperty(String name) {
        return PROPERTY_LOADER_FROM_SYSTEM_GET_PROPERTY.getOptionalProperty(name);
    }

    public static <T> Optional<T> getOptionalSystemProperty(String name, PropertyValueParser<T> parser) {
        return PROPERTY_LOADER_FROM_SYSTEM_GET_PROPERTY.getOptionalProperty(name, parser);
    }

    public static <T> Optional<Stream<T>> getOptionalSystemProperty(String name, PropertyValueParser<Stream<String>> splitter,
                                                                    PropertyValueParser<T> elementParser) {
        return PROPERTY_LOADER_FROM_SYSTEM_GET_PROPERTY.getOptionalProperty(name, splitter, elementParser);
    }


    private static <T> T optionalToRequired(String name, Optional<T> optional) {
        return optional.orElseThrow(() -> new PropertyIsNotDefinedException(name));
    }

    private static <T> T invokeParser(PropertyValueParser<T> parser, String name, String value) {
        try {
            return parser.parse(value);
        } catch (Exception e) {
            throw new PropertyValueParsingException(name, value, e);
        }
    }


    @FunctionalInterface
    public interface PropertyValueParser<T> {
        T parse(String value) throws Exception;
    }


    public static class PropertyIsNotDefinedException extends RuntimeException {
        public PropertyIsNotDefinedException(String name) {
            super(String.format("Property '%s' is not defined", name));
        }
    }

    public static class PropertyValueParsingException extends RuntimeException {
        public PropertyValueParsingException(String name, String value, Exception e) {
            super(String.format("Can't parse value of property '%s': '%s': %s", name, value, e.getMessage()), e);
        }
    }
}
