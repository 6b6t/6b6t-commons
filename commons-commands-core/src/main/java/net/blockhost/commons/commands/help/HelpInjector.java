package net.blockhost.commons.commands.help;

import com.mojang.brigadier.Command;
import lombok.experimental.UtilityClass;

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
}
