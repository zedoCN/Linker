package top.zedo.net.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import top.zedo.util.HexPrinter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class JsonPacket extends BasePacket {
    private final byte[] bytes; //Json数据
    private final JSONObject jsonObject; //Json

    public JsonPacket(JSONObject data) {
        jsonObject = data;
        JSONWriter writer = JSONWriter.ofJSONB();
        writer.write(data);
        bytes = writer.getBytes();
    }

    @Override
    public String toString() {
        return "JsonPacket{" +
                "jsonObject=" + jsonObject +
                '}';
    }

    public JsonPacket(ByteBuffer packData) {
        bytes = new byte[packData.remaining()];
        packData.get(bytes);
        JSONReader jsonReader = JSONReader.ofJSONB(bytes);
        jsonObject = jsonReader.readJSONObject();
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    @Override
    protected void build(ByteBuffer buffer) {
        buffer.put(bytes);
    }

    @Override
    protected int getDataSize() {
        return bytes.length;
    }

    @Override
    protected PacketType getType() {
        return PacketType.JSON;
    }
}
