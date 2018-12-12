package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lengxia on 2018/11/28.
 */

@Entity
@Table(name = "`binary`")
public class Binary extends Model {
    @Id
    public Integer id;

    public  String name;

    public String type;

    public Date t_create;

    public byte[] data;

    public static Find<Integer, Binary> finder =
            new Find<Integer, Binary>(){};
}
