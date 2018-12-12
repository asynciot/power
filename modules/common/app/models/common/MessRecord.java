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
public class MessRecord  extends Model{

    @Id
    public Integer id;

    public String title;

    public String content;

    @Column(nullable=false)
    public Integer type = 0; //0 == wechat, 1 == sms

    @Column(length=32, nullable=false)
    public String toId;

    public Integer device_id;

    public String info;

    @Column(nullable=false)
    public Date createTime;


    public static Find<Integer, MessRecord> finder =
            new Find<Integer, MessRecord>(){};
}
