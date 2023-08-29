package top.zedo.util;

import java.nio.ByteBuffer;

public class HexPrinter {
    public static void printHex(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        int position = buffer.position();
        int limit = buffer.limit();
        sb.append("ByteBuffer:").append(buffer).append("\n");
        for (int i = 0; i < limit; i++) {
            sb.append(String.format("%02X ", buffer.get(i)));
        }

        System.out.println(sb); // 换行
        buffer.position(position);//返回之前的位置
    }

    public static void printHex(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println(sb); // 换行
    }

}
