package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
public class Repair  extends  Model{
    @Id
    public Integer id;
    public Integer device_id;
    public String userId;

    public  String fault_id;

    public String state;

    public String phone;

    public Date create_time;

    public Date finish_time;

    public static Find<Integer, Repair> finder =
            new Find<Integer, Repair>(){};
}
