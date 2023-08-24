package data;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class MessagePack extends JSONPack {
    @Override
    public String toString() {
        return "MessagePack{" +
                "message=" + getMessage() +
                '}' ;
    }
    public MessagePack(String message) {
        data.put("message", message);
    }
    public String getMessage() {
        return data.getString("message");
    }
    public MessagePack(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public char getTypeIndex() {
        return 2;
    }


}
