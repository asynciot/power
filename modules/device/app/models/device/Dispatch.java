package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
public class Dispatch extends  Model{
    @Id
    public Integer id;

    public Integer device_id;
    public String  user_id;
    public Integer  order_id;

    public Integer order_type;
    public String state;
    public String phone;

    public String result;

    public String create_time;
    public String finish_time;
    public String confirm_time;

    public String before_pic;
    public String after_pic;
    public String confirm_pic;


    public static Find<Integer, Dispatch> finder =
            new Find<Integer, Dispatch>(){};
}
