package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Runtime extends Model {
    @Id
    public Integer id;

    public  Integer device_id;

    public Integer type;

    public byte[] data;
    public Date t_update;

    public static Find<Integer, Runtime> finder =
            new Find<Integer, Runtime>(){};
}