package old.data;

public enum EventType {
    GROUP_CREAT("用户离开了组"),
    GROUP_DISSOLVE("主机解散了组"),
    GROUP_JOIN("用户加入了组"),
    GROUP_LEAVE("用户离开了组"),
    GET_GROUPS(""),
    CHANNEL_CONNECT(""),
    CHANNEL_DISCONNECT(""),
    USER_GET_IDENTITY(""),
    USER_GET_GROUP_MEMBER(""),
    ;
    final public String info;

    EventType(String info) {
        this.info = info;
    }
}
