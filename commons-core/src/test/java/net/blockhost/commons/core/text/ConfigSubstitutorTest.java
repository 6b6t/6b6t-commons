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

        String result = ConfigSubstitutor.substitute(
                "java: ${sys:java.version}, os: ${sys:os.name}");

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
        String javaVersion = System.getProperty("java.version");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", "${sys:java.version}");
        data.put("count", 42);
        data.put("enabled", true);

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        assertEquals(javaVersion, result.get("version"));
        assertEquals(42, result.get("count"));
        assertEquals(true, result.get("enabled"));
    }

    @Test
    void substituteValues_resolvesNestedMaps() {
        String javaVersion = System.getProperty("java.version");
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("java-version", "${sys:java.version}");
        nested.put("port", 3306);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("database", nested);

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        @SuppressWarnings("unchecked")
        Map<String, Object> resultNested = (Map<String, Object>) result.get("database");
        assertEquals(javaVersion, resultNested.get("java-version"));
        assertEquals(3306, resultNested.get("port"));
    }

    @Test
    void substituteValues_resolvesValuesInLists() {
        String javaVersion = System.getProperty("java.version");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", List.of("${sys:java.version}", "literal", "${sys:os.name}"));

        Map<String, Object> result = ConfigSubstitutor.substituteValues(data);

        @SuppressWarnings("unchecked")
        List<String> items = (List<String>) result.get("items");
        assertEquals(javaVersion, items.get(0));
        assertEquals("literal", items.get(1));
        assertEquals(System.getProperty("os.name"), items.get(2));
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
