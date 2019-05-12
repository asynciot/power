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
public class Roles extends Model{
    @Id
    public String id;

    @Column(nullable = false,length=20)
    public String name;

    public String menus;

    public String functions;

    public static Find<String, Roles> finder =
            new Find<String, Roles>(){};

}
