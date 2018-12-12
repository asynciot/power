package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * Created by lengxia on 2018/11/17.
 */
@Entity
public class IPlocation extends Model {
    @Id
    public Integer id;

    public String ip;

    public String area;

    public String area_id;

    public String  city;

    public String city_id;

    public String country;

    public String country_id;

    public String county;

    public String county_id;

    public String isp;

    public String region;

    public String region_id;

    public static Find<Integer, IPlocation> finder =
            new Find<Integer, IPlocation>(){};
}
