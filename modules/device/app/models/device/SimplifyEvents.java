package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class SimplifyEvents extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public String event_type;

    public String device_type;

    public Date start_time;

    public Date end_time;

    public Integer current;

    public Integer speed;

    public Integer door;

    public Integer max_door;

    public static Find<Integer, SimplifyEvents> finder =
            new Find<Integer, SimplifyEvents>(){};
}