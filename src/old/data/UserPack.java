package old.data;

import old.group.LinkerUser;

import java.nio.ByteBuffer;

public class UserPack extends JSONPack {
    public UserPack(LinkerUser user) {
        data.put("user", GroupPack.getUserJSON(user));
    }

    public LinkerUser getUser() {
        return GroupPack.getUserObject(data.getJSONObject("user"));
    }

    public UserPack(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public char getTypeIndex() {
        return 4;
    }
}
