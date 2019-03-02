package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


/**
 * Created by lengxia on 2018/3/2
 */
@Entity
public class Group extends Model {
    @Id
    public Integer id;

    public String leader;

    public String name;

    public Integer mobile;

    public Date create_time;

    public String region;

    public static Find<Integer, Group> finder =
            new Find<Integer, Group>(){};
}
