package net.blockhost.commons.core.text;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Utility for substituting environment variables and Java system properties in strings.
///
/// Supports two substitution patterns:
/// - `${env:VAR_NAME}` - resolves the environment variable `VAR_NAME`
/// - `${sys:property.name}` - resolves the Java system property `property.name`
///
/// Unrecognized patterns (no matching prefix or nonexistent variable) are left as-is.
///
/// ## Usage
///
/// ```java
/// // Simple string substitution
/// String result = ConfigSubstitutor.substitute("host: ${env:DB_HOST}");
///
/// // Recursive map substitution (for raw YAML data)
/// Map<String, Object> data = RawYamlLoader.load(path);
/// Map<String, Object> resolved = ConfigSubstitutor.substituteValues(data);
/// ```
///
/// Only `env:` and `sys:` lookups are enabled. Other Commons Text lookups
/// (such as `url:`, `script:`, `dns:`) are intentionally excluded for security.
@UtilityClass
public class ConfigSubstitutor {

    /// Creates a new [StringSubstitutor] configured with environment variable
    /// and system property lookups.
    ///
    /// @return a new substitutor instance
    public StringSubstitutor createSubstitutor() {
        Map<String, StringLookup> lookups = Map.of(
                "env", StringLookupFactory.INSTANCE.environmentVariableStringLookup(),
                "sys", StringLookupFactory.INSTANCE.systemPropertyStringLookup());
        StringLookup lookup = StringLookupFactory.INSTANCE.interpolatorStringLookup(lookups, null, false);
        return new StringSubstitutor(lookup);
    }

    /// Substitutes environment variables and system properties in the given string.
    ///
    /// @param input the string to process
    /// @return the string with all recognized `${env:...}` and `${sys:...}` patterns resolved
    /// @throws NullPointerException if input is null
    public String substitute(String input) {
        Objects.requireNonNull(input, "input");
        return createSubstitutor().replace(input);
    }

    /// Recursively substitutes environment variables and system properties
    /// in all string values within a map structure.
    ///
    /// Non-string values (numbers, booleans, etc.) are left unchanged.
    /// Nested maps and lists are processed recursively.
    ///
    /// @param data the map to process
    /// @return a new map with all string values substituted
    /// @throws NullPointerException if data is null
    public Map<String, Object> substituteValues(Map<String, Object> data) {
        Objects.requireNonNull(data, "data");
        StringSubstitutor substitutor = createSubstitutor();
        return substituteMap(data, substitutor);
    }

    private Map<String, Object> substituteMap(Map<String, Object> data, StringSubstitutor substitutor) {
        Map<String, Object> result = new LinkedHashMap<>(data.size());
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result.put(entry.getKey(), substituteValue(entry.getValue(), substitutor));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object substituteValue(Object value, StringSubstitutor substitutor) {
        if (value instanceof String str) {
            return substitutor.replace(str);
        }
        if (value instanceof Map<?, ?> map) {
            return substituteMap((Map<String, Object>) map, substitutor);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(item -> substituteValue(item, substitutor)).toList();
        }
        return value;
    }
}
