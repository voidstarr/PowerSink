package tv.voidstar.powersink.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import tv.voidstar.powersink.Constants;
import tv.voidstar.powersink.PowerSink;
import tv.voidstar.powersink.PowerSinkData;
import tv.voidstar.powersink.energy.EnergyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ListExecutor implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        List<Text> contents = new ArrayList<>();

        Optional<User> argUser = args.getOne("player");

        UUID searchPlayer = null;
        boolean enableRemove = false;

        if (argUser.isPresent()) {
            if(!src.hasPermission(Constants.LIST_NODES_OTHER_PERMISSION)) {
                src.sendMessage(Text.of("you don't have permission to list other player's energynodes."));
                return CommandResult.success();
            }
            searchPlayer = argUser.get().getUniqueId();
            enableRemove = src.hasPermission(Constants.REMOVE_NODES_OTHER_PERMISSION);
        } else {
            if(!src.hasPermission(Constants.LIST_NODES_SELF_PERMISSION)) {
                src.sendMessage(Text.of("you don't have permission to list your energynodes."));
                return CommandResult.success();
            }
            if(src instanceof Player) {
                searchPlayer = ((Player) src).getUniqueId();
                enableRemove = src.hasPermission(Constants.REMOVE_NODES_SELF_PERMISSION);
            }
        }

        UUID finalSearchPlayer = searchPlayer;
        boolean finalEnableRemove = enableRemove;
        boolean canTeleport = src.hasPermission("minecraft.command.tp");
        PowerSinkData.getEnergyNodes().forEach(((location, energyNode) -> {
            if (energyNode.getPlayerOwner() == finalSearchPlayer)
                contents.add(nodeText(energyNode, finalEnableRemove, canTeleport));
        }));

        PaginationList.builder()
                .title(Text.of("PowerSink node list"))
                .contents(contents)
                .padding(Text.of("-"))
                .sendTo(src);

        return CommandResult.success();
    }

    private Text nodeText(EnergyNode energyNode, boolean remove, boolean teleport) {
        Text owner = Text.of(PowerSink.getUserStorageService().get(energyNode.getPlayerOwner()).get().getName().concat("'s "));

        Text nodeType = Text.of(energyNode.getNodeType().toString());

        Text.Builder locationTextBuilder = Text.builder(" [location]").color(TextColors.DARK_BLUE)
                .onHover(TextActions.showText(Text.of(energyNode.getLocation())));

        Text locationText = null;
        if(teleport) {
            String teleportCommand = "/tp "
                    + energyNode.getLocation().getX() + " "
                    + energyNode.getLocation().getY() + " "
                    + energyNode.getLocation().getZ();
            locationText = locationTextBuilder.onClick(TextActions.runCommand(teleportCommand)).build();
        } else {
            locationText = locationTextBuilder.build();
        }

        Text.Builder builder = Text.builder().append(owner, nodeType, locationText);

        if (remove) {
            Text removeText = Text.builder(" [remove]").color(TextColors.DARK_BLUE)
                    .onClick(
                            TextActions.executeCallback((cmdSrc) -> {
                                PowerSinkData.delEnergyNode(energyNode.getLocation());
                                Text.builder().append(Text.of("Removed "), owner, Text.of("'s Energy"), nodeType).build();
                                PowerSink.sendMessage(Text.builder().append(Text.of("Removed "), owner, Text.of("'s Energy"), nodeType).build().toPlain(), (Player) cmdSrc);
                            })
                    )
                    .onHover(TextActions.showText(Text.of("Remove this node.")))
                    .build();

            builder.append(removeText);
        }

        return builder.build();
    }
}
