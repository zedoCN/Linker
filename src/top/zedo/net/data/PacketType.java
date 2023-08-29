package top.zedo.net.data;

public enum PacketType {
    CHANNEL((byte) 0),
    JSON((byte) 1);

    private final byte mark;

    PacketType(byte mark) {
        this.mark = mark;

    }

    public byte getMark() {
        return mark;
    }


}
