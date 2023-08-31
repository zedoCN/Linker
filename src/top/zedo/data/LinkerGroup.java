package top.zedo.data;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONType;

import java.util.HashMap;
import java.util.UUID;

@JSONType(ignores = {"UUID"})
public class LinkerGroup {
    public String name;//组名称
    private UUID uuid;//组uuid
    public LinkerUser host;//主机uuid
    public long createTime;//创建日期
    public HashMap<UUID, LinkerUser> members = new HashMap<>();//组所有成员
    public long uuidMost = 0; // UUID最高有效位
    public long uuidLeast = 0; // UUID最低有效位



    public UUID getUUID() {
        if (uuid == null)
            uuid = new UUID(uuidMost, uuidLeast);
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
        uuidMost = uuid.getMostSignificantBits();
        uuidLeast = uuid.getLeastSignificantBits();
    }

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
        members.put(host.getUUID(), host);
        this.host = host;
        host.group=this;
        createTime = System.currentTimeMillis();
    }

    public static LinkerGroup build(JSONObject jsonObject) {
        return jsonObject.to(LinkerGroup.class);
    }

    public JSONObject toJSON() {
        return JSONObject.from(this);
    }
}
