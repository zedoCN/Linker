package top.zedo.user;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.UUID;

public class LinkerUser {
    public String name;//用户名称
    public UUID uuid;//用户uuid
    public UUID group;//加入的组uuid

    @Override
    public String toString() {
        return "LinkerUser{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                ", group=" + group +
                '}';
    }

    public LinkerUser(String name) {
        this.name = name;
        uuid = UUID.randomUUID();
    }


}
