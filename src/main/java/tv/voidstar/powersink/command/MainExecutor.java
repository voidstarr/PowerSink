package tv.voidstar.powersink.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MainExecutor implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        List<Text> contents = new ArrayList<>();

        contents.add(Text.of("/ps list <player> - list all nodes, sinks or sources"));
        contents.add(Text.of("/ps remove [index] - remove a specific node from PowerSink"));

        PaginationList.builder()
                .title(Text.of("PowerSink command list"))
                .contents(contents)
                .padding(Text.of("-"))
                .sendTo(src);

        return CommandResult.success();
    }
}
