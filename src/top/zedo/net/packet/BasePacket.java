package top.zedo.net.packet;

import java.nio.ByteBuffer;

public abstract class BasePacket {

    public ByteBuffer buildPack() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(getDataSize() + 5);
        byteBuffer.putInt(getDataSize() + 1);
        byteBuffer.put(getType().getMark());
        build(byteBuffer);
        byteBuffer.flip();
        return byteBuffer;
    }

    /**
     * 解析数据包
     *
     * @param buffer 数据包数据
     */
    public static BasePacket resolved(ByteBuffer buffer) {
        byte type = buffer.get();
        if (type >= PacketType.values().length || type <= -1) {
            throw new IllegalArgumentException("数据包类型无效");
        }
        return switch (PacketType.values()[type]) {
            case CHANNEL -> new ChannelPacket(buffer);
            case JSON -> new JsonPacket(buffer);
        };
    }

    /**
     * 构建数据包
     *
     * @param buffer 在此buffer增加数据
     */
    protected abstract void build(ByteBuffer buffer);

    /**
     * 获取数据区大小
     *
     * @return 数据区大小
     */
    protected abstract int getDataSize();

    /**
     * 获取包类型
     *
     * @return 包标识
     */
    protected abstract PacketType getType();
}
