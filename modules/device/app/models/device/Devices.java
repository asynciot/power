package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Devices extends Model {
    @Id
    public Integer id;

    public Date t_create;

    public Date t_update;

    public Date t_logon;

    public Date t_logout;

    public Integer dock_id;

    public String board;

    public String cellular;

    public String firmware;

    @Column(nullable = false,unique = true)
    public String IMEI;

    public String IMSI;

    public String device;

    public String model;

    public byte[] contract_id;

    public Integer cell_mcc;

    public Integer cell_mnc;

    public Integer cell_lac;

    public Integer cell_cid;

    public String ipaddr;

    public static Find<Integer, Devices> finder =
            new Find<Integer, Devices>(){};
}