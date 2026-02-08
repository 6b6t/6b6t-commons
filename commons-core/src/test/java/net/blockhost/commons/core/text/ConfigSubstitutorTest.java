package net.blockhost.commons.core.text;

import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSubstitutorTest {

    @Test
    void substitute_resolvesSystemProperty() {
        String javaVersion = System.getProperty("java.version");

        String result = ConfigSubstitutor.substitute("version: ${sys:java.version}");

        assertEquals("version: " + javaVersion, result);
    }

    @Test
    void substitute_resolvesEnvironmentVariable() {
        // PATH/Path is available on all platforms
        String path = System.getenv().entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase("PATH"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(path, "PATH environment variable should exist");

        String envKey = System.getenv().keySet().stream()
                .filter(k -> k.equalsIgnoreCase("PATH"))
                .findFirst()
                .orElseThrow();

        String result = ConfigSubstitutor.substitute("path: ${env:" + envKey + "}");

        assertEquals("path: " + path, result);
    }

    @Test
    void substitute_leavesUnrecognizedPatternsAsIs() {
        String input = "value: ${unknown:something}";

        String result = ConfigSubstitutor.substitute(input);

        assertEquals(input, result);
    }

    @Test
    void substitute_leavesNonPrefixedPatternsAsIs() {
        String input = "value: ${notALookup}";

        String result = ConfigSubstitutor.substitute(input);

        assertEquals(input, result);
    }

    @Test
    void substitute_handlesStringWithoutPatterns() {
        String input = "plain text without any patterns";

        String result = ConfigSubstitutor.substitute(input);

        assertEquals(input, result);
    }

    @Test
    void substitute_handlesMixedEnvAndSysPatterns() {
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");

        String result = ConfigSubstitutor.substitute("java: ${sys:java.version}, os: ${sys:os.name}");

        assertEquals("java: " + javaVersion + ", os: " + osName, result);
    }

    @Test
    void substitute_nullInput_throwsNpe() {
        assertThrows(NullPointerException.class, () -> ConfigSubstitutor.substitute(null));
    }

    @Test
    void substitute_emptyString_returnsEmpty() {
        assertEquals("", ConfigSubstitutor.substitute(""));
    }

    @Test
    void createSubstitutor_returnsWorkingInstance() {
        StringSubstitutor substitutor = ConfigSubstitutor.createSubstitutor();

        assertNotNull(substitutor);

        String javaVersion = System.getProperty("java.version");
        assertEquals(javaVersion, substitutor.replace("${sys:java.version}"));
    }

    @Test
    void substituteValues_resolvesStringValuesInMap() {
        String osName = System.getProperty("os.name");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("os", "${sys:os.name}");
        data.put("count", 42);
        data.put("enabled", true);

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        assertEquals(osName, result.get("os"));
        assertEquals(42, result.get("count"));
        assertEquals(true, result.get("enabled"));
    }

    @Test
    void substituteValues_resolvesNestedMaps() {
        String osName = System.getProperty("os.name");
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("os-name", "${sys:os.name}");
        nested.put("port", 3306);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("database", nested);

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        @SuppressWarnings("unchecked")
        Map<String, Object> resultNested = (Map<String, Object>) result.get("database");
        assertEquals(osName, resultNested.get("os-name"));
        assertEquals(3306, resultNested.get("port"));
    }

    @Test
    void substituteValues_resolvesValuesInLists() {
        String osName = System.getProperty("os.name");
        String userDir = System.getProperty("user.dir");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", List.of("${sys:os.name}", "literal", "${sys:user.dir}"));

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) result.get("items");
        assertEquals(osName, items.get(0));
        assertEquals("literal", items.get(1));
        assertEquals(userDir, items.get(2));
    }

    @Test
    void substituteValues_returnsNewMap_doesNotMutateOriginal() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("value", "${sys:java.version}");

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        assertNotSame(data, result);
        assertEquals("${sys:java.version}", data.get("value"));
    }

    @Test
    void substituteValues_nullInput_throwsNpe() {
        assertThrows(NullPointerException.class, () -> ConfigSubstitutor.substituteValues(null));
    }

    @Test
    void substituteValues_emptyMap_returnsEmptyMap() {
        Map<String, Object> result = ConfigSubstitutor.substituteValues(Map.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void substituteValues_coercesSubstitutedIntegerStrings() {
        System.setProperty("test.port", "3306");
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("port", "${sys:test.port}");

            Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

            assertEquals(3306, result.get("port"));
            assertInstanceOf(Integer.class, result.get("port"));
        } finally {
            System.clearProperty("test.port");
        }
    }

    @Test
    void substituteValues_coercesSubstitutedBooleanStrings() {
        System.setProperty("test.enabled", "true");
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("enabled", "${sys:test.enabled}");

            Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

            assertEquals(true, result.get("enabled"));
            assertInstanceOf(Boolean.class, result.get("enabled"));
        } finally {
            System.clearProperty("test.enabled");
        }
    }

    @Test
    void substituteValues_coercesSubstitutedDoubleStrings() {
        System.setProperty("test.ratio", "3.14");
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("ratio", "${sys:test.ratio}");

            Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

            assertEquals(3.14, result.get("ratio"));
            assertInstanceOf(Double.class, result.get("ratio"));
        } finally {
            System.clearProperty("test.ratio");
        }
    }

    @Test
    void substituteValues_doesNotCoerceNonSubstitutedStrings() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("value", "3306");

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        assertEquals("3306", result.get("value"));
        assertInstanceOf(String.class, result.get("value"));
    }

    @Test
    void substitute_doesNotResolveScriptLookup() {
        // Verify that dangerous lookups like script: are not available
        String input = "${script:javascript:java.lang.Runtime.getRuntime().exec('echo')}";

        String result = ConfigSubstitutor.substitute(input);

        assertEquals(input, result);
    }

    @Test
    void substitute_doesNotResolveUrlLookup() {
        String input = "${url:UTF-8:https://example.com}";

        String result = ConfigSubstitutor.substitute(input);

        assertEquals(input, result);
    }

    @Test
    void substitute_doesNotResolveDnsLookup() {
        String input = "${dns:address|example.com}";

        String result = ConfigSubstitutor.substitute(input);

        assertEquals(input, result);
    }
}
