package util;

import java.nio.ByteBuffer;

public class ByteBufferHexPrinter {
    public static void printByteBufferAsHex(ByteBuffer buffer) {
        int position = buffer.position();
        int limit = buffer.limit();

        for (int i = position; i < limit; i++) {
            System.out.printf("%02X ", buffer.get(i));
        }

        System.out.println(); // 换行
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put((byte) i);
        }

        printByteBufferAsHex(buffer);
    }
}
