package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Events extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public Date time;
    @Column(name = "`length`")
    public Integer length;
    @Column(name = "`interval`")
    public Integer interval;

    public byte[] data;
    public static Find<Integer, Events> finder =
            new Find<Integer, Events>(){};
}