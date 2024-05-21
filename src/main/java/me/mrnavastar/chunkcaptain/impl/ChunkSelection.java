package me.mrnavastar.chunkcaptain.impl;

import lombok.Getter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ChunkSelection implements Cloneable {

    private ServerWorld world;
    private NbtList data = new NbtList();
    private final ChunkPos selectionPoint;

    public ChunkSelection(ServerWorld world, List<Chunk> chunks) {
        this.world = world;
        chunks.forEach(chunk -> {
            NbtCompound chunkData = ChunkSerializer.serialize(world, chunk);
            chunkData.remove("structures");
            data.add(chunkData);
            System.out.println(ChunkSerializer.serialize(world, chunk).toString());
        });

        NbtCompound f = (NbtCompound) data.getFirst();
        selectionPoint = new ChunkPos(f.getInt("xPos"), f.getInt("zPos"));
    }

    public int getSize() {
        return data.size();
    }

    //TODO: Make this work lol
    public void shuffle() {
        /*List<ChunkPos> positions = new ArrayList<>();
        data.forEach(chunk -> {
            NbtCompound c = (NbtCompound) chunk;
            positions.add(new ChunkPos(c.getInt("xPos"), c.getInt("xPos")));
        });
        Collections.shuffle(positions);

        data.forEach(chunk -> {
            NbtCompound c = (NbtCompound) chunk;
            ChunkPos pos = positions.removeFirst();
            c.putInt("xPos", pos.x);
            c.putInt("zPos", pos.z);
        });*/
    }

    @Override
    public ChunkSelection clone() {
        try {
            ChunkSelection clone = (ChunkSelection) super.clone();
            clone.world = world;
            clone.data = data.copy();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}