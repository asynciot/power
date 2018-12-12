package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Logs extends Model {
    @Id
    public Integer id;

    public Integer dock_id;

    public Integer device_id;

    public Date time;


    public static Find<Integer, Logs> finder =
            new Find<Integer, Logs>(){};
}