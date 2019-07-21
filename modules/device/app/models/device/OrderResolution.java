package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2019/1/19.
 */
@Entity
public class OrderResolution extends Model{
    @Id
    public Integer id;

    public String reason;

    public String answer;

    public String code_id;

    public String sign;

    public static Find<Integer, OrderResolution> finder =
            new Find<Integer, OrderResolution>(){};
}
