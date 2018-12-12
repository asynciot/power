package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/11/27.
 */
@Entity
public class Monitor extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public Integer session;

    public Integer sequence;
    @Column(name = "`length`")
    public Integer length;
    @Column(name = "`interval`")
    public Integer interval;

    public Date time;

    public byte[] data;

    public static Find<Integer, Monitor> finder =
            new Find<Integer, Monitor>(){};
}
