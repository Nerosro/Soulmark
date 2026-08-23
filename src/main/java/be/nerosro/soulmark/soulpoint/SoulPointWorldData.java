package be.nerosro.soulmark.soulpoint;

import be.nerosro.soulmark.SoulMark;
import be.nerosro.soulmark.SoulmarkConfig;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Freezes the configured starting Soul Point balance for a world.
 */
public class SoulPointWorldData extends SavedData {

    private static final SavedDataType<SoulPointWorldData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "soul_points"),
            SoulPointWorldData::new,
            Codec.INT.xmap(SoulPointWorldData::new, SoulPointWorldData::getStartingSoulPoints)
    );

    private final int startingSoulPoints;

    public SoulPointWorldData() {
        this(SoulmarkConfig.SOUL_POINT_STARTING_BALANCE.get());
    }

    private SoulPointWorldData(int startingSoulPoints) {
        this.startingSoulPoints = startingSoulPoints;
    }

    public static SoulPointWorldData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public int getStartingSoulPoints() {
        return startingSoulPoints;
    }
}