package data;

import com.alibaba.fastjson2.JSONObject;
import group.LinkerUser;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EventPack extends JSONPack {
    @Override
    public String toString() {
        return "EventPack{" +
                "data=" + data +
                '}';
    }

    public EventType getType() {
        return EventType.valueOf(data.getString("eventType"));
    }

    public UUID getSourceUser() {
        return UUID.fromString(data.getString("sourceUser"));
    }

    public UUID getSourceGroup() {
        return UUID.fromString(data.getString("sourceGroup"));
    }

    public String getParameter() {
        return data.getString("parameter");
    }

    public JSONObject getParameterJSON() {
        return data.getJSONObject("parameterJSON");
    }

    public EventPack(ByteBuffer buffer) {
        super(buffer);
    }

    public EventPack(EventType eventType, LinkerUser user, String parameter) {
        data.put("eventType", eventType.name());
        data.put("sourceUser", user.uuid);
        if (user.group != null)
            data.put("sourceGroup", user.group.uuid);
        data.put("parameter", parameter);
    }

    public EventPack(EventType eventType, LinkerUser user, String parameter, JSONObject parameterJSON) {
        data.put("eventType", eventType.name());
        data.put("sourceUser", user.uuid);
        if (user.group != null)
            data.put("sourceGroup", user.group.uuid);
        data.put("parameter", parameter);
        data.put("parameterJSON", parameterJSON);
    }

    @Override
    public char getTypeIndex() {
        return 3;
    }
}
