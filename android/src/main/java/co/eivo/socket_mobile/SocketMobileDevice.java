package co.eivo.socket_mobile;

import java.util.HashMap;

public class SocketMobileDevice {

    public String name;
    public String uuid;
    public String guid;

    SocketMobileDevice(String name, String uuid, String guid) {
        this.name = name;
        this.uuid = uuid;
        this.guid = guid;
    }

    public HashMap<String, String> toMap() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", this.name);
        map.put("uuid", this.uuid);
        map.put("guid", this.guid);

        return map;
    }
}