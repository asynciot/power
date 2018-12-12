package models.device;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Credentials extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public byte[] credential;

    public static Find<Integer, Credentials> finder =
            new Find<Integer, Credentials>(){};
 }