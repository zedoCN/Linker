package top.zedo.data;

public enum LinkerEvent {
    USER_LOGIN,//用户登录
    USER_LEAVE,//用户离开
    USER_JOIN_GROUP,//用户加入组
    USER_LEAVE_GROUP,//用户离开组
    HOST_DISSOLVE_GROUP,//主机解散组
    GROUP_GET_MESSAGES,//在群里获得消息
    USER_GET_START,//用户获得状态
    USER_GET_GROUPS,//用户获得组列表
    GROUP_UPDATE,//组更新
    CHANNEL_CLOSE,//通道断开
    COMMAND_SUCCESS,//命令执行成功
}
