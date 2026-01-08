package net.blockhost.commons.commands.help;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HelpCommandWrapper<S> implements Command<S> {
    private final Command<S> delegate;
    @Getter
    private final String description;
    @Getter
    private final boolean privateCommand;

    @Override
    public int run(CommandContext<S> commandContext) throws CommandSyntaxException {
        return delegate.run(commandContext);
    }
}
