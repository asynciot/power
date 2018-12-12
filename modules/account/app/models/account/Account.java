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
 * Created by lengxia on 2018/10/30.
 */

@Entity
public class Account extends Model{
    @Id
    public String id;

    @Column(nullable = false)
    public  String username;

    @JsonIgnore
    public  String password;

    public String mobile;

    public String wechat;

    public String wechat_id;

    public Boolean sex;

    @Formats.DateTime(pattern="yyyy-MM-dd")
    public Date birthday;

    public String getBirthday() {
        if (null != birthday) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(birthday);
        }

        return null;
    }

    public String profession;

    public String introduction;

    public String email;

    @Index
    public Date createTime;

    @JsonIgnore
    @Transient
    public Integer verifyCode;

    public static Find<String, Account> finder =
            new Find<String, Account>(){};

}
