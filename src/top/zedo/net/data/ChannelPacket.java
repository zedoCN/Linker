package top.zedo.net.data;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ChannelPacket extends BasePacket {
    public UUID fromUser;//来自用户的uuid
    public UUID toUser;//到用户的uuid
    public UUID uuid;//连接通道的uuid
    public ByteBuffer sourceData;//源数据

    public ChannelPacket(UUID fromUser, UUID toUser, UUID uuid, ByteBuffer sourceData) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.uuid = uuid;
        this.sourceData = sourceData;
    }

    /**
     * 通过原始包数据构建
     *
     * @param packData 原始包数据
     */
    public ChannelPacket(ByteBuffer packData) {
        // Read fromUser UUID
        long fromMostSignificantBits = packData.getLong();
        long fromLeastSignificantBits = packData.getLong();
        fromUser = new UUID(fromMostSignificantBits, fromLeastSignificantBits);

        // Read toUser UUID
        long toMostSignificantBits = packData.getLong();
        long toLeastSignificantBits = packData.getLong();
        toUser = new UUID(toMostSignificantBits, toLeastSignificantBits);

        // Read channel uuid
        long uuidMostSignificantBits = packData.getLong();
        long uuidLeastSignificantBits = packData.getLong();
        uuid = new UUID(uuidMostSignificantBits, uuidLeastSignificantBits);

        // Read sourceData
        sourceData = packData.slice();
    }

    @Override
    public String toString() {
        return "ChannelPacket{" +
                "fromUser=" + fromUser +
                ", toUser=" + toUser +
                ", uuid=" + uuid +
                ", sourceDataSize=" + sourceData.limit() +
                '}';
    }

    @Override
    protected void build(ByteBuffer buffer) {
        // Write fromUser UUID
        buffer.putLong(fromUser.getMostSignificantBits());
        buffer.putLong(fromUser.getLeastSignificantBits());

        // Write toUser UUID
        buffer.putLong(toUser.getMostSignificantBits());
        buffer.putLong(toUser.getLeastSignificantBits());

        // Write uuid
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }

    @Override
    protected int getDataSize() {
        return 3 * 16 + sourceData.limit();
    }

    @Override
    protected PacketType getType() {
        return PacketType.CHANNEL;
    }
}
