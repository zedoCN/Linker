package data;

import java.nio.ByteBuffer;

public class InitPack extends JSONPack {


    public InitPack(ByteBuffer buffer) {
        super(buffer);
    }

    public InitPack(String name) {
        data.put("name", name);
    }


    public String getName() {
        return data.getString("name");
    }

    @Override
    public char getTypeIndex() {
        return 1;
    }
}
