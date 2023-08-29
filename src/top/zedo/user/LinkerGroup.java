package top.zedo.user;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class LinkerGroup {
    public String name;//组名称
    public UUID uuid;//组uuid
    public HashMap<UUID, LinkerUser> members = new HashMap<>();//组所有成员

    @Override
    public String toString() {
        return "LinkerGroup{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                ", members=" + members +
                '}';
    }

    public LinkerGroup(String name, LinkerUser host) {
        this.name = name;
        uuid = UUID.randomUUID();
        members.put(host.uuid, host);
    }
}
