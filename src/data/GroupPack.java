package data;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import group.LinkerGroup;
import group.LinkerUser;

import java.nio.ByteBuffer;
import java.util.UUID;

public class GroupPack extends JSONPack {
    public LinkerGroup group;

    public GroupPack(LinkerGroup linkerGroup) {
        data.put("host", getUserJSON(linkerGroup.host));
        data.put("name", linkerGroup.name);
        data.put("uuid", linkerGroup.uuid);
        JSONArray jsonArray = new JSONArray();
        for (LinkerUser user : linkerGroup.users) {
            jsonArray.add(getUserJSON(user));
        }
        data.put("users", jsonArray);
    }

    @Override
    public String toString() {
        return "GroupPack{" +
                "group=" + group +
                '}';
    }

    public static JSONObject getUserJSON(LinkerUser user) {
        JSONObject json = new JSONObject();
        json.put("name", user.name);
        json.put("uuid", user.uuid);
        json.put("timestamp", user.timestamp);
        return json;
    }

    public static LinkerUser getUserObject(JSONObject json) {
        LinkerUser user = new LinkerUser(null);
        user.name = json.getString("name");
        user.uuid = UUID.fromString(json.getString("uuid"));
        user.timestamp = json.getLongValue("timestamp");
        return user;
    }

    public GroupPack(ByteBuffer buffer) {
        super(buffer);
        group = new LinkerGroup(getUserObject(data.getJSONObject("host")), data.getString("name"), UUID.fromString(data.getString("uuid")));
        JSONArray userArray = data.getJSONArray("users");
        for (int i = 0; i < userArray.size(); i++) {
            group.users.add(getUserObject(userArray.getJSONObject(i)));
        }
    }

    public LinkerGroup getGroup() {
        return group;
    }

    @Override
    public char getTypeIndex() {
        return 5;
    }
}
