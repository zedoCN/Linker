package top.zedo.data;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class LinkerGroup {
    public String name;//组名称
    public UUID uuid;//组uuid
    public LinkerUser host;//主机uuid
    public HashMap<UUID, LinkerUser> members = new HashMap<>();//组所有成员

    @Override
    public String toString() {
        return "LinkerGroup{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                ", host=" + host.name +
                ", members=" + members +
                '}';
    }

    public LinkerGroup(String name, LinkerUser host) {
        this.name = name;
        uuid = UUID.randomUUID();
        members.put(host.getUUID(), host);
        this.host = host;
    }

    public static LinkerGroup build(JSONObject jsonObject) {
        return jsonObject.to(LinkerGroup.class);
    }

    public JSONObject toJSON() {
        return JSONObject.from(this);
    }
}
