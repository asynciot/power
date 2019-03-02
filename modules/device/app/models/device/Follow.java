package models.device;

import com.avaje.ebean.Model;
import org.omg.CORBA.PUBLIC_MEMBER;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
public class Follow extends Model {
    @Id
    public Integer id;

    public String userId;

    public String imei;

    public Integer device_id;

    public Date createTime;

    public String ftype;

    public static Find<Integer, Follow> finder =
            new Find<Integer, Follow>(){};
}
