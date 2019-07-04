package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


/**
 * Created by lengxia on 2018/3/2
 */
@Entity
public class Organization extends Model {
    @Id
    public Integer id;

    public String leader;

    public String name;

    public Date t_create;

    public String region;
	
	public String logo;
	
	public String bg1;
	
	public String bg2;
	
	public String bg3;

    public String organize_id;

    public Integer number;

    public static Find<Integer, Organization> finder =
            new Find<Integer, Organization>(){};
}
