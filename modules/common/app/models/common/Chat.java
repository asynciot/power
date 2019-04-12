package models.common;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
public class Chat extends Model {

    @Id
    public Integer id;


    public String title;

    public String content;
	public Integer follow;
    @Column(nullable=false)
    public Integer type = 0; //0 == 系统消息, 1 == 用户消息

    @Column(length=32, nullable=false)
    public String fromId;

    public String info;

    @Column(nullable=false)
    public Date create_time;

    public static Find<Integer, Chat> finder =
            new Find<Integer, Chat>(){};

}
