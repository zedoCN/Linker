package group;

import data.BasePack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LinkerGroup {
    public String name;
    public LinkerUser host;//主机
    public Set<LinkerUser> users;//用户
    public HashMap<UUID, ProxyChannel> proxyChannels = new HashMap<>();
    public UUID uuid = UUID.randomUUID();


    public void sendPack(BasePack pack) {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LinkerUser user : users) {
            sb.append("[").append(user.name).append("] ");
        }

        return "LinkerGroup{" +
                "name='" + name + '\'' +
                ", host=" + host.name +
                ", users=" + sb +
                ", uuid=" + uuid +
                '}';
    }

    public LinkerGroup(LinkerUser host, String name) {
        this.host = host;
        users = new HashSet<>();
        this.name = name;
    }

    public LinkerGroup(LinkerUser host, String name, UUID uuid) {
        this.host = host;
        users = new HashSet<>();
        this.name = name;
        this.uuid = uuid;
    }
}
