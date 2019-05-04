package models.account;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.JsonIgnore;
import play.data.format.Formats;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Created by k on 2019/4/24.
 */

@Entity
public class Functions extends Model{
    @Id
    public String id;

    @Column(nullable = false)
    public String name;

    public Boolean monitor;

    public Boolean memory;

    public Boolean work_audt;

    public Boolean work_order;

    public Boolean work_up;

    public Boolean work_dispatch;

    public Boolean update_devices;

    public Boolean rem_devices;

    public Boolean info_evelution;

    public Boolean new_ladder;

    public Boolean rem_ladder;

    public Boolean update_ladder;

    public Boolean new_user;

    public Boolean update_user;

    public Boolean rem_user;

    public Boolean new_roles;

    public Boolean rem_roles;

    public Boolean update_roles;

    public Boolean assign_roles;

    public Boolean print;

    public static Find<String, Functions> finder =
            new Find<String, Functions>(){};

}
