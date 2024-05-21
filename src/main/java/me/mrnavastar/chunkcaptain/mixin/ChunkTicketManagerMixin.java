package me.mrnavastar.chunkcaptain.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.mrnavastar.chunkcaptain.impl.ChunkSwapper;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkTicketManager.class)
public class ChunkTicketManagerMixin implements ChunkSwapper {

    @Shadow @Final private Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition;

    @Override
    public void chunkCaptain$swap(ProtoChunk protoChunk) {
        ticketsByPosition.remove(protoChunk.getPos().toLong());
    }
}
