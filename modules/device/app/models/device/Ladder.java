package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Ladder extends Model {
    @Id
    public Integer id;

    public String name;

    public Date t_create;

    public Date t_update;

    public Date t_logon;

    public Date t_logout;

    public Integer cell_mcc;

    public Integer cell_mnc;

    public Integer cell_lac;

    public Integer cell_cid;

    public String ipaddr;

    public String ctrl;

    public String door1;

    public String door2;

    public String install_addr;

    public String state;

    public static Find<Integer, Ladder> finder =
            new Find<Integer, Ladder>(){};
}