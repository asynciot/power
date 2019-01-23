package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2019/1/19.
 */
@Entity
public class Artlocation extends Model{
    @Id
    public Integer id;

    public String lat;

    public String lon;

    public String user_id;

    public String t_create;
    public static Find<Integer, Artlocation> finder =
            new Find<Integer, Artlocation>(){};
}
