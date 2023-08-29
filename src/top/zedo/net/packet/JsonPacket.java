package top.zedo.net.packet;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import top.zedo.data.LinkerCommand;
import top.zedo.data.LinkerEvent;

import java.nio.ByteBuffer;

public class JsonPacket extends BasePacket {
    private final byte[] bytes; //Json数据
    private final JSONObject jsonObject; //Json

    public JsonPacket(JSONObject data) {
        jsonObject = data;
        JSONWriter writer = JSONWriter.ofJSONB();
        writer.write(data);
        bytes = writer.getBytes();
    }

    public static JsonPacket buildCommandPacket(JSONObject json, LinkerCommand command) {
        JSONObject root = new JSONObject();
        root.put("type", "command");
        root.put("name", command.name());
        root.put("value", json);
        return new JsonPacket(root);
    }

    public static JsonPacket buildEventPacket(JSONObject json, LinkerEvent event) {
        JSONObject root = new JSONObject();
        root.put("type", "event");
        root.put("name", event.name());
        root.put("value", json);
        return new JsonPacket(root);
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
