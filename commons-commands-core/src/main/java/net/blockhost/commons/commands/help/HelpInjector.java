package net.blockhost.commons.commands.help;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Method;

@UtilityClass
public class HelpInjector {
    public static <S> Command<S> wrapCommand(Command<S> command, Method commandMethod) {
        CommandDescription descriptionAnnotation = commandMethod.getAnnotation(CommandDescription.class);
        PrivateCommand privateCommandAnnotation = commandMethod.getAnnotation(PrivateCommand.class);
        if (descriptionAnnotation == null && privateCommandAnnotation == null) {
            return command;
        }

        return new HelpCommandWrapper<>(
                command,
                descriptionAnnotation != null ? descriptionAnnotation.value() : "No description provided.",
                privateCommandAnnotation != null);
    }

    public static <S extends Audience> void sendDefaultHelp(CommandContext<S> context) {
        var source = context.getSource();
        var command = context.getInput();
        var dispatcher = new CommandDispatcher<S>();
        var parseContext = dispatcher.parse(command, source).getContext();
        if (!parseContext.getNodes().isEmpty()) {
            var lastNode = parseContext.getNodes().getLast();
            var smartUsage = dispatcher.getSmartUsage(lastNode.getNode(), source);
            if (!smartUsage.isEmpty()) {
                source.sendMessage(Component.text("Did you mean:"));
                for (var usage : smartUsage.values()) {
                    source.sendMessage(Component.text("/%s %s".formatted(command, usage)));
                }
            }
        }
    }
}
