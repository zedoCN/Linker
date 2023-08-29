package old.data;

import java.nio.ByteBuffer;
import java.util.UUID;

public class DataPack extends BasePack {
    public ByteBuffer sourceData;
    public UUID uuid;

    @Override
    public String toString() {
        return "DataPack{size=" + sourceData.limit() + "}";
    }

    public DataPack(ByteBuffer data) {
        uuid = new UUID(data.getLong(), data.getLong());
        sourceData = data.slice(16, data.limit() - 16);
    }

    public DataPack(UUID uuid, ByteBuffer sourceData) {
        this.sourceData = sourceData;
        this.uuid = uuid;
    }

    @Override
    public char getTypeIndex() {
        return 6;
    }

    @Override
    protected ByteBuffer build() {
        ByteBuffer packData;
        packData = ByteBuffer.allocateDirect(sourceData.limit() + 16);
        packData.putLong(uuid.getMostSignificantBits());
        packData.putLong(uuid.getLeastSignificantBits());
        packData.put(sourceData);
        return packData;
    }
}
