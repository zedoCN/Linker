package top.zedo.data;

import com.alibaba.fastjson2.JSONObject;
import top.zedo.net.packet.JsonPacket;

public enum LinkerCommand {
    PING,//延迟检测
    CHANGE_NAME,//修改昵称
    SEND_GROUP_MESSAGE,//发送群消息
    ;

}
