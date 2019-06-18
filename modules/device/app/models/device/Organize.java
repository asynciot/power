package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Organize extends Model {
    @Id
    public Integer id;

    public String leader;

    public String name;

    public String mobile;

    public Date t_create;

    public String region;

    public String group_id;

    public static Find<Integer, Organize> finder =
            new Find<Integer, Organize>(){};
}
