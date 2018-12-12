
package Sms.common;

import com.avaje.ebean.Ebean;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.BizResult;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;
import controllers.common.CodeException;
import controllers.common.CodeGenerator;
import controllers.common.ErrDefinition;
import models.common.SmsRecord;

import java.util.Calendar;
import java.util.Date;

public class SmsManager {
    private static SmsManager s_instance = null;
    private static int EXPIRED_TIME = 1;
    private static int RESEND_EXPIRED_TIME = 5;
    private final static String SERVER_URL = "http://gw.api.taobao.com/router/rest";
    private final static String APP_KEY =  "23342836";
    private final static String APP_SECRET = "62e2ed6a79def266512801cebf5fca94";

    public static SmsManager getInstance() {
        if (null == s_instance) {
            s_instance = new SmsManager();
        }

        return s_instance;
    }

    private SmsManager() {

    }

    public void sendVerification(String mobile) throws Throwable {
        SmsRecord record = getRecord(mobile);

        if (null != record) {
            //deny to send code during effective time
            throw new CodeException(ErrDefinition.E_SMS_ALREADY_SENT);
        }

        Integer code = CodeGenerator.random.nextInt(10000);

        //send message
        TaobaoClient client = new DefaultTaobaoClient(SERVER_URL, APP_KEY, APP_SECRET);
        AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
        req.setSmsType("normal");
        req.setSmsFreeSignName("注册验证");
        req.setSmsParamString(String.format("{\"code\":\"%04d\", \"product\":\"宁波申菱\"}", code));
        req.setRecNum(mobile);
        req.setSmsTemplateCode("SMS_7170079");
        AlibabaAliqinFcSmsNumSendResponse rsp = client.execute(req);

        BizResult result = rsp.getResult();
        if (result.getSuccess()) {
            //save message record
            record = new SmsRecord();
            record.mobile = mobile;
            record.code = String.format("%04d", code);
            record.timestamp = new Date();
            Ebean.save(record);
        }
    }

    public boolean verifyCode(String mobile, String code) {
        SmsRecord record =  getRecord(mobile, code);

        return record == null ? false : true;
    }
    
    private SmsRecord getRecord(String mobile, String code) {
        Long number = Long.parseLong(mobile);
        Date currentTime = new Date();
        Calendar expiredTime = Calendar.getInstance();
        expiredTime.setTime(currentTime);
        expiredTime.add(Calendar.MINUTE, -RESEND_EXPIRED_TIME);
        SmsRecord record = SmsRecord.find.where()
                .between("timestamp", expiredTime.getTime(), currentTime)
                .eq("mobile", number)
                .eq("code", code)
                .setMaxRows(1)
                .orderBy("timestamp desc")
                .findUnique();

        return record;
    }

    private SmsRecord getRecord(String mobile) {
        Long number = Long.parseLong(mobile);

        Date currentTime = new Date();
        Calendar expiredTime = Calendar.getInstance();
        expiredTime.setTime(currentTime);
        expiredTime.add(Calendar.MINUTE, -EXPIRED_TIME);
        SmsRecord record = SmsRecord.find.where()
                .between("timestamp", expiredTime.getTime(), currentTime)
                .eq("mobile", number)
                .setMaxRows(1)
                .orderBy("timestamp desc")
                .findUnique();

        return record;
    }
    
}