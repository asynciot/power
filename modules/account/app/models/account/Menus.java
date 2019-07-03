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
public class Menus extends Model{
    @Id
    public String id;

    @Column(nullable = false)
    public String name;

    public Boolean dashboard;

    public Boolean menu;

    public Boolean map;

    public Boolean laddermap;

    public Boolean maintain;

    public Boolean auditinglist;

    public Boolean maintainlist;

    public Boolean event;

    public Boolean allist;

    public Boolean evolution;

    public Boolean ladder;
    
    public Boolean ele_group;

    public Boolean organize;

    public Boolean sys;

    public Boolean user_manage;

    public Boolean inform;

    public Boolean authority;

    public Boolean setting;

    public Boolean print;

    public static Find<String, Menus> finder =
            new Find<String, Menus>(){};

}
