package net.blockhost.commons.commands.help;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.RootCommandNode;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

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

    public static <S, A extends Audience> void sendDefaultHelp(CommandContext<S> context, Function<S, A> audienceMapper) {
        var source = context.getSource();
        var command = context.getInput();
        var root = (RootCommandNode<S>) context.getRootNode();
        var dispatcher = new CommandDispatcher<S>(root);
        var parseContext = dispatcher.parse(command, source).getContext();
        if (!parseContext.getNodes().isEmpty()) {
            var lastNode = parseContext.getNodes().getLast();
            var smartUsage = dispatcher.getSmartUsage(lastNode.getNode(), source);
            if (!smartUsage.isEmpty()) {
                var audience = audienceMapper.apply(source);
                audience.sendMessage(Component.text("Did you mean:"));
                for (var entries : smartUsage.entrySet()) {
                    var commandNode = entries.getKey();
                    if (!(commandNode.getCommand() instanceof HelpCommandWrapper<?> helpWrapper) || helpWrapper.privateCommand()) {
                        continue;
                    }
                    var usage = entries.getValue();
                    audience.sendMessage(Component.text("/%s %s - %s".formatted(command, usage, helpWrapper.description())));
                }
            }
        }
    }
}
