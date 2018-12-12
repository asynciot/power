package models.common;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class SmsRecord extends Model {

    @Id
    public Integer id;
    
    @Index
    @Column(nullable=false)
    public String mobile;
    
    @Column(nullable=false,length=4)
    public String code;
    
    @Column(nullable=false)
    public Date timestamp;
    
    public static Find<Integer, SmsRecord> find =
            new Find<Integer, SmsRecord>(){};
}
