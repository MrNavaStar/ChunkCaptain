package me.mrnavastar.chunkcaptain.mixin;

import it.unimi.dsi.fastutil.longs.LongSet;
import me.mrnavastar.chunkcaptain.impl.ChunkSwapper;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin implements ChunkSwapper {

    @Unique
    private final ArrayList<ProtoChunk> chunksToSwap = new ArrayList<>();
    @Unique
    private final ArrayList<ProtoChunk> chunksToSend = new ArrayList<>();
    @Unique
    private int ticks = 0;

    @Shadow @Final
    private LongSet unloadedChunks;

    @Shadow @Nullable protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final private ThreadedAnvilChunkStorage.TicketManager ticketManager;

    @Shadow public abstract List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos pos);

    @Shadow @Final private ServerWorld world;

    @Inject(method = "unloadChunks", at = @At("HEAD"))
    private void unloadChunks(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        chunksToSwap.forEach(chunk -> {
            ChunkHolder holder = getChunkHolder(chunk.getPos().toLong());
            if (holder == null) return;

            ((ChunkSwapper) ticketManager).chunkCaptain$swap(chunk);
            holder.setCompletedChunk((WrapperProtoChunk) chunk);
            unloadedChunks.add(chunk.getPos().toLong());
        });
        chunksToSend.addAll(chunksToSwap);
        chunksToSwap.clear();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ticks++;
        if (chunksToSend.isEmpty() || ticks % 4 == 0) return;
        ticks = 0;

        Chunk chunk = chunksToSend.getFirst();
        getPlayersWatchingChunk(chunk.getPos()).forEach(playerEntity -> {
            playerEntity.networkHandler.sendPacket(new UnloadChunkS2CPacket(chunk.getPos()));
            playerEntity.networkHandler.sendPacket(new ChunkDataS2CPacket(((WrapperProtoChunk) chunk).getWrappedChunk(), world.getLightingProvider(), null, null));
        });
    }

    @Unique
    public void chunkCaptain$swap(ProtoChunk protoChunk) {
        chunksToSwap.add(protoChunk);
    }
}
