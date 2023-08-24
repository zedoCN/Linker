package data;

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

    @Override
    public char getTypeIndex() {
        return 3;
    }
}
