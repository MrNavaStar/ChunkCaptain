package me.mrnavastar.chunkcaptain;

import me.mrnavastar.chunkcaptain.impl.ChunkCommands;
import me.mrnavastar.chunkcaptain.impl.ChunkSelection;
import me.mrnavastar.chunkcaptain.impl.ChunkSwapper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChunkCaptain implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(ChunkCommands::registerCommand);
    }

    public static ChunkSelection selectChunk(ServerWorld world, ChunkPos pos) {
        return new ChunkSelection(world, Collections.singletonList(world.getChunk(pos.x, pos.z)));
    }

    public static ChunkSelection selectChunk(ServerWorld world, BlockPos pos) {
        return selectChunk(world, ChunkSectionPos.from(pos).toChunkPos());
    }

    public static ChunkSelection selectChunks(ServerWorld world, ChunkPos pos1, ChunkPos pos2) {
        List<Chunk> chunks = new ArrayList<>();
        for (int x = Math.min(pos1.x, pos2.x); x <= Math.max(pos1.x, pos2.x); x++) {
            for (int z = Math.min(pos1.z, pos2.z); z <= Math.max(pos1.z, pos2.z); z++) {
               chunks.add(world.getChunk(x, z));
            }
        }
        return new ChunkSelection(world, chunks);
    }

    public static ChunkSelection selectChunks(ServerWorld world, BlockPos pos1, BlockPos pos2) {
        return selectChunks(world, ChunkSectionPos.from(pos1).toChunkPos(), ChunkSectionPos.from(pos2).toChunkPos());
    }

    public static void pasteSelection(ServerWorld world, ChunkSelection selection, ChunkPos pos) {
        int deltaX = pos.x - selection.getSelectionPoint().x;
        int deltaZ = pos.z - selection.getSelectionPoint().z;

        selection.getData().forEach(chunk -> {
            NbtCompound c = (NbtCompound) chunk.copy();
            ChunkPos newPos = new ChunkPos(((NbtCompound) chunk).getInt("xPos") + deltaX, ((NbtCompound) chunk).getInt("zPos") + deltaZ);
            c.putInt("xPos", newPos.x);
            c.putInt("zPos", newPos.z);

            ProtoChunk protoChunk = ChunkSerializer.deserialize(world, world.getPointOfInterestStorage(), newPos, c);
            ((ChunkSwapper) world.getChunkManager().threadedAnvilChunkStorage).chunkCaptain$swap(protoChunk);
        });
    }

    public static void pasteSelection(ServerWorld world, ChunkSelection selection, BlockPos pos) {
        pasteSelection(world, selection, ChunkSectionPos.from(pos).toChunkPos());
    }
}