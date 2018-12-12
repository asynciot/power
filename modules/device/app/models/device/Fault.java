package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
public class Fault extends Model {

    @Id
    public Integer id;

    public Integer device_id;

    public Integer type;

    public Date createTime;

    public String state;



    public static Find<Integer, Fault> finder =
            new Find<Integer, Fault>(){};
}
