package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/11/23.
 */
@Entity
public class DeviceInfo extends Model{
    @Id
    public Integer id;

    public String IMEI;

    public Integer IPlocation_id;

    public Integer cellocation_id;

    public String device_name;

    public String register;

    public String tagcolor;

    public String state;

    public String device_type;

    public String commond;

    public String delay;

    public  Integer rssi;

    public static Find<Integer, DeviceInfo> finder =
            new Find<Integer, DeviceInfo>(){};
}
