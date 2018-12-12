package services.device;

import services.device.SendMonitorThread;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by lengxia on 2018/11/29.
 */
@Singleton
public class DeviceService {
    @Inject
    public DeviceService() {
        new Thread( new SendMonitorThread()).start();

    }
}
