package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/10/30.
 */
@Entity
public class Commands extends Model{
    @Id
    public  Integer id;

    public String IMEI;

    public  String command;

    @Column(name = "`int1`")
    public  Integer int1;

    @Column(name = "`int2`")
    public  Integer int2;

    @Column(name = "`int3`")
    public  Integer int3;

    @Column(name = "`int4`")
    public  Integer int4;

    public  String str1;

    public  String str2;

    public  String str3;

    public  String str4;

    public  byte[] contract;

    public  Integer binary_id;

    public  String result;

    public  Date submit;

    public  Date execute;

    public Date finish;

    @Column(name = "`binary`")
    public  byte[] binary;

    public static Find<Integer, Commands> finder =
            new Find<Integer, Commands>(){};

}
