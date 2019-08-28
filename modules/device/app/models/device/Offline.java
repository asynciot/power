package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
@Table(name =  "`offline`")
public class Offline extends Model {

    @Id
    public Integer id;

    public Integer device_id;

    public String t_logout;

    public String duration;

    public static Find<Integer, Offline> finder =
            new Find<Integer, Offline>(){};
}