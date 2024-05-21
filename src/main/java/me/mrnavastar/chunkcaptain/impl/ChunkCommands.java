package me.mrnavastar.chunkcaptain.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.mrnavastar.chunkcaptain.ChunkCaptain;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.server.command.ResetChunksCommand.executeResetChunks;

public class ChunkCommands {

    private static final ConcurrentHashMap<UUID, ChunkSelection> selections = new ConcurrentHashMap<>();

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("chunkcaptain").requires(source -> source.hasPermissionLevel(4)).requires(ServerCommandSource::isExecutedByPlayer)

                .then(CommandManager.literal("select")
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(ChunkCommands::selectChunk)
                                .then(CommandManager.argument("pos2", BlockPosArgumentType.blockPos())
                                        .executes(ChunkCommands::selectChunks)
                                )
                        )
                )

                .then(CommandManager.literal("paste")
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(ChunkCommands::pasteChunks)
                        )
                )

                .then(CommandManager.literal("clear").executes(ChunkCommands::clearSelection))
                .then(CommandManager.literal("shuffle").executes(ChunkCommands::shuffleSelection))
                .then(CommandManager.literal("reset").executes((context) -> executeResetChunks(context.getSource(), 0, true)))
        );
    }

    private static int selectChunk(CommandContext<ServerCommandSource> context) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        selections.put(context.getSource().getPlayer().getUuid(), ChunkCaptain.selectChunk(context.getSource().getWorld(), pos));
        context.getSource().sendMessage(Text.of("Chunks Selected"));
        return 0;
    }

    private static int selectChunks(CommandContext<ServerCommandSource> context) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        BlockPos pos2 = BlockPosArgumentType.getBlockPos(context, "pos2");
        selections.put(context.getSource().getPlayer().getUuid(), ChunkCaptain.selectChunks(context.getSource().getWorld(), pos, pos2));
        context.getSource().sendMessage(Text.of("Chunks Selected"));
        return 0;
    }

    private static int pasteChunks(CommandContext<ServerCommandSource> context) {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        ChunkSelection selection = selections.get(context.getSource().getPlayer().getUuid());
        if (selection == null) {
            context.getSource().sendMessage(Text.of("No Chunks Selected"));
            return 1;
        }

        ChunkCaptain.pasteSelection(context.getSource().getWorld(), selection, pos);
        context.getSource().sendMessage(Text.of("Pasted Chunks"));
        return 0;
    }

    private static int clearSelection(CommandContext<ServerCommandSource> context) {
        selections.remove(context.getSource().getPlayer().getUuid());
        context.getSource().sendMessage(Text.of("No Chunks Selected"));
        return 0;
    }

    private static int shuffleSelection(CommandContext<ServerCommandSource> context) {
        ChunkSelection selection = selections.get(context.getSource().getPlayer().getUuid());
        if (selection == null) {
            context.getSource().sendMessage(Text.of("No Chunks Selected"));
            return 1;
        }
        selection.shuffle();
        context.getSource().sendMessage(Text.of("Shuffled Chunks"));
        return 0;
    }
}
