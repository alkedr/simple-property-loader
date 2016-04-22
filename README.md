# simple-property-loader

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.alkedr/simple-property-loader/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.alkedr/simple-property-loader)
[![Build Status](https://travis-ci.org/alkedr/simple-property-loader.svg?branch=master)](https://travis-ci.org/alkedr/simple-property-loader)

A simpler alternative to [QATools properties-loader](http://mvnrepository.com/artifact/ru.yandex.qatools.properties/properties-loader), [OWNER](http://owner.aeonbits.org/) and simular libraries.

### Examples

All examples assume you have
```import static com.github.alkedr.SimplePropertyLoader.*;```

##### Get optional `String` property
```
Optional<String> x = getOptionalSystemProperty("my.property");
```

##### Get non-optional `String` property (throws exception if property isn't set)
```
String x = getSystemProperty("my.property");
```

##### Get `Long` property
```
Long x = getSystemProperty("my.property", Long::valueOf);
```

##### Get property with default value
```
Optional<String> x = getOptionalSystemProperty("my.property").orElse("default value");
```

##### Get `Long[]` property (comma separated list of numbers)
```
Long[] x = getSystemProperty("my.property", Pattern.compile(",")::splitAsStream, Long::valueOf);
```

##### TODO: example: loading properties from file
