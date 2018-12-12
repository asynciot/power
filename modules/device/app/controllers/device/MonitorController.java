package controllers.device;

import controllers.common.BaseController;
import play.mvc.LegacyWebSocket;
import play.mvc.WebSocket;

import java.util.Date;

/**
 * Created by lengxia on 2018/11/27.
 */
public class MonitorController extends BaseController {
    public LegacyWebSocket<String> socket(final String deviceId){
        //final String userId = session().get("userId");
        String userId= session("userId");
        return WebSocket.whenReady((in, out) -> {
            SocketManager.getInstance().joinRoom(deviceId, userId, out);
            in.onClose(()->SocketManager.getInstance().leave(deviceId, userId));

        });
    }
}
