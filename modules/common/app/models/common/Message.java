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
public class Message extends Model {

    @Id
    public Integer id;


    public String title;

    public String content;

    @Column(nullable=false)
    public Integer type = 0; //0 == 系统消息, 1 == 用户消息

    @Column(length=32, nullable=false)
    public String toId;

    @Column(length=32, nullable=false)
    public String fromId;

    public String info;

    @Column(nullable=false)
    public Date createTime;

    @Column(nullable=false)
    public Boolean isSettled = false;

    public static Find<Integer, Message> finder =
            new Find<Integer, Message>(){};

}
