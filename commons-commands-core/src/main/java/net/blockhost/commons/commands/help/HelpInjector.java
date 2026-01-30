package net.blockhost.commons.commands.help;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

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
                _ -> Component.text(
                        descriptionAnnotation != null ? descriptionAnnotation.value() : "No description provided."),
                privateCommandAnnotation != null);
    }

    public static <S, A extends Audience> void sendDefaultHelp(
            CommandContext<S> context, Function<S, A> audienceMapper) {
        var source = context.getSource();
        var audience = audienceMapper.apply(source);
        var command = context.getInput();
        var root = (RootCommandNode<S>) context.getRootNode();
        var dispatcher = new CommandDispatcher<S>(root);
        var parseContext = dispatcher.parse(command, source).getContext();
        if (parseContext.getNodes().isEmpty()) {
            audience.sendMessage(Component.text("Invalid command!"));
            return;
        }

        var parsedRange = parseContext.getRange().get(command);
        var lastNode = parseContext.getNodes().getLast();
        var allUsage = getAllUsage(root, lastNode.getNode(), source);
        if (allUsage.isEmpty()) {
            audience.sendMessage(Component.text("Invalid command!"));
            return;
        }

        audience.sendMessage(Component.text("Invalid command! Did you mean:"));
        for (var usage : allUsage) {
            var helpWrapper = unwrapDelegates(usage.node().getCommand());
            if (helpWrapper.isEmpty() || helpWrapper.get().privateCommand()) {
                continue;
            }
            audience.sendMessage(Component.text("/%s %s - ".formatted(parsedRange, usage.usage))
                    .append(helpWrapper.get().description().apply(source)));
        }
    }

    private static <S> List<CommandUsage<S>> getAllUsage(
            final RootCommandNode<S> root, final CommandNode<S> node, final S source) {
        final List<CommandUsage<S>> result = new ArrayList<>();
        getAllUsage(root, node, source, result, "");
        return result;
    }

    private static <S> void getAllUsage(
            final RootCommandNode<S> root,
            final CommandNode<S> node,
            final S source,
            final List<CommandUsage<S>> result,
            final String prefix) {
        if (!node.canUse(source)) {
            return;
        }

        if (node.getCommand() != null) {
            result.add(new CommandUsage<>(node, prefix));
        }

        if (node.getRedirect() != null) {
            final String redirect = Objects.equals(node.getRedirect(), root)
                    ? "..."
                    : "-> " + node.getRedirect().getUsageText();
            result.add(new CommandUsage<>(
                    node,
                    prefix.isEmpty()
                            ? node.getUsageText() + CommandDispatcher.ARGUMENT_SEPARATOR + redirect
                            : prefix + CommandDispatcher.ARGUMENT_SEPARATOR + redirect));
        } else if (!node.getChildren().isEmpty()) {
            for (final CommandNode<S> child : node.getChildren()) {
                getAllUsage(
                        root,
                        child,
                        source,
                        result,
                        prefix.isEmpty()
                                ? child.getUsageText()
                                : prefix + CommandDispatcher.ARGUMENT_SEPARATOR + child.getUsageText());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <S> Optional<HelpCommandWrapper<S>> unwrapDelegates(@Nullable Command<S> command) {
        while (command != null) {
            if (command instanceof HelpCommandWrapper<S> helpWrapper) {
                return Optional.of(helpWrapper);
            }

            var delegateField = Arrays.stream(command.getClass().getDeclaredFields())
                    .filter(field -> field.getType().isAssignableFrom(Command.class))
                    .filter(field -> field.getName().equals("delegate"))
                    .findFirst();
            if (delegateField.isEmpty()) {
                break;
            }

            delegateField.get().setAccessible(true);
            try {
                command = (Command<S>) delegateField.get().get(command);
            } catch (IllegalAccessException e) {
                break;
            }
        }

        return Optional.empty();
    }

    private record CommandUsage<S>(CommandNode<S> node, String usage) {}
}
