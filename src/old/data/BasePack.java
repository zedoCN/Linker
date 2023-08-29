package old.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class BasePack {
    public int size;

    public BasePack() {
    }

    public static BasePack analyzePack(ByteBuffer buffer) {
        if (buffer.remaining() < 16) {
            System.out.println("服务器:协议错误");
            return null;
        }

        byte[] verify = new byte["LINKER".length()];
        int size = buffer.getInt() - 12;
        buffer.get(verify);
        if (new String(verify, StandardCharsets.US_ASCII).equals("LINKER")) {

            char type = buffer.getChar();

            //System.out.println("数据包:" + Integer.toString(type) + " 大小:" + size);
            ByteBuffer packData = buffer.slice(12, size);
            //System.out.println(packData);
            BasePack pack = switch (type) {
                case 1 -> new InitPack(packData);
                case 2 -> new MessagePack(packData);
                case 3 -> new EventPack(packData);
                case 4 -> new UserPack(packData);
                case 5 -> new GroupPack(packData);
                case 6 -> new DataPack(packData);
                default -> null;
            };
            if (pack != null) {
                pack.size = size + 12;
            }
            return pack;

        }
        System.out.println("服务器:未知类型");
        return null;
    }

    public ByteBuf buildData(ByteBufAllocator byteBufAllocator) {
        ByteBuffer byteBuffer = build();
        ByteBuf data = byteBufAllocator.buffer(byteBuffer.limit() + 12);
        data.writeInt(byteBuffer.limit() + 12);
        data.writeBytes("LINKER".getBytes(StandardCharsets.US_ASCII));
        data.writeChar(getTypeIndex());
        byteBuffer.flip();
        data.writeBytes(byteBuffer);
        return data;
    }

    public abstract char getTypeIndex();

    protected abstract ByteBuffer build();

    @Override
    public String toString() {
        return "Pack{" + getClass() + "}";
    }
}
