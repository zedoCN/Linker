package top.zedo.data;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;

import java.util.UUID;

@JSONType(ignores = {"UUID", "group"})
public class LinkerUser {
    public String name;//用户名称
    private UUID uuid;//用户uuid
    public LinkerGroup group;//加入的组
    public int delay = 0; // 延迟
    public String ipAddress = "未知"; // IP 地址
    public long loginTime = 0; // 登录时间
    public long upTraffic = 0; // 上行流量
    public long downTraffic = 0; // 下行流量
    public long totalUpBytes = 0; // 总上行字节数
    public long totalDownBytes = 0; // 总下行字节数
    public long previousTotalUpBytes = 0; // 上一秒的总上行字节数
    public long previousTotalDownBytes = 0; // 上一秒的总下行字节数
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

    /**
     * 更新流量信息 (每隔1秒更新)
     */
    public void calculateTraffic() {
        // 计算上行流量
        upTraffic = totalUpBytes - previousTotalUpBytes;
        previousTotalUpBytes = totalUpBytes; // 更新上一秒的总上行字节数
        // 计算下行流量
        downTraffic = totalDownBytes - previousTotalDownBytes;
        previousTotalDownBytes = totalDownBytes; // 更新上一秒的总上行字节数
    }

    @Override
    public String toString() {
        return "LinkerUser{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                ", group=" + (group == null ? "" : group.name) +
                '}';
    }


    public LinkerUser() {
        name = null;
        group = null;
    }

    /**
     * 是否在组里
     */
    public boolean isInGroup() {
        return group != null;
    }

    /**
     * 是否是主机
     */
    public boolean isHost() {
        if (!isInGroup())
            return false;
        return group.host.getUUID().equals(uuid);
    }

    public static LinkerUser build(JSONObject jsonObject) {
        return jsonObject.to(LinkerUser.class);
    }

    public JSONObject toJSON() {
        return JSONObject.from(this);
    }

}
