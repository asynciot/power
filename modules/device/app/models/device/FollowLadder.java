package models.device;

import com.avaje.ebean.Model;
import org.omg.CORBA.PUBLIC_MEMBER;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
public class FollowLadder extends Model {
    @Id
    public Integer id;

    public String userId;

    public String ctrl;

    public String door1;

    public String door2;

    public Integer ladder_id;

    public Date createTime;

    public static Find<Integer, FollowLadder> finder =
            new Find<Integer, FollowLadder>(){};
}
