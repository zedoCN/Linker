package old.data;

import com.alibaba.fastjson2.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class JSONPack extends BasePack {
    public JSONObject data;


    public JSONPack() {
        data = new JSONObject();
    }

    public JSONPack(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        data = JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8));
    }



    @Override
    protected ByteBuffer build() {
        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        return buffer;
    }

}
