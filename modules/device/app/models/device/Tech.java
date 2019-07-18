package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2019/1/19.
 */
@Entity
public class Tech extends Model{
    @Id
    public Integer id;

    public String name;

    public String reason;

    public String answer;

    public static Find<Integer, Tech> finder =
            new Find<Integer, Tech>(){};
}
