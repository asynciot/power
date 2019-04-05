package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Ladder extends Model {
    @Id
    public Integer id;

    public String ctrl_id;

    public String name;

    public String ctrl;

    public String door1;

    public String door2;

    public String install_addr;

    public String state;

    public static Find<Integer, Ladder> finder =
            new Find<Integer, Ladder>(){};
}