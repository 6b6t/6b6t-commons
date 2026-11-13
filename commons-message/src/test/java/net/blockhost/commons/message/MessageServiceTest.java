package net.blockhost.commons.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageServiceTest {
    @Test
    void parsesColorsAndComponentPlaceholders() {
        assertEquals(Component.text("Hello", NamedTextColor.GREEN), MessageService.parse("<green>Hello"));
        Component name = Component.text("Alex", NamedTextColor.GOLD);
        assertEquals(name, MessageService.parse("<name>", MessageService.placeholder("name", name)));
    }

    @Test
    void keepsUnparsedInputLiteral() {
        assertEquals(
                Component.text("<red>Alex"), MessageService.parse("<name>", Placeholder.unparsed("name", "<red>Alex")));
    }

    @Test
    void resolvesConfiguredMessagesAndMissingKeys() {
        Map<String, String> messages = Map.of("welcome", "<name>");
        MessageService service = MessageService.create(messages::get);
        assertEquals(Component.text("Alex"), service.get("welcome", Map.of("name", "Alex")));
        assertEquals(Component.text("<Missing message: absent>"), service.get("absent"));
    }

    @Test
    void sendsTheResolvedComponentToTheAudience() {
        Audience audience = mock(Audience.class);
        MessageService service = MessageService.create(key -> "<green>Hello");
        service.send(audience, "welcome");
        verify(audience).sendMessage(Component.text("Hello", NamedTextColor.GREEN));
    }

    @Test
    void legacyConfigurationRetainsItsMeaningAfterSerialization() {
        MiniMessageConfigSerializer serializer = new MiniMessageConfigSerializer();
        String converted = serializer.deserialize("&aHello");
        assertEquals(Component.text("Hello", NamedTextColor.GREEN), MessageService.parse(converted));
        assertEquals(converted, serializer.deserialize(serializer.serialize(converted)));
    }
}
