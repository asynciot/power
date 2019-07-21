package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2019/1/19.
 */
@Entity
public class OrderCode extends Model{
    @Id
    public Integer id;

    public String name;

    public String code;

    public String resolution_id;

    public static Find<Integer, OrderCode> finder =
            new Find<Integer, OrderCode>(){};
}
