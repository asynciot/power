package controllers.device;

import com.avaje.ebean.Ebean;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Binary;
import models.device.Commands;
import models.device.DeviceInfo;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by lengxia on 2018/10/30.
 */
public class CommandController extends BaseController {
    @Inject
    private FormFactory formFactory;
    public static int session_id=1;
    public Result create() {
        return create(Commands.class, formFactory);
    }
    public Result read(){
        return read(Commands.class, formFactory);
    }
    public Result delete() {
        return delete(Commands.class, formFactory);
    }

    public Result register()throws NoSuchAlgorithmException {
        DynamicForm form=formFactory.form().bindFromRequest();
        String imei  =form.get("IMEI");
        String imsi= form.get("IMSI");
        String op=form.get("op");
        try{
            if(imei==null||imei.isEmpty()||imsi==null||imsi.isEmpty()||op==null||op.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMOND_REGISTER_INCORRECT_PARAM);
            }
            DeviceInfo deviceInfo=DeviceInfo.finder.where().eq("IMEI",imei).findUnique();
            if(!deviceInfo.commond.equals("ok")){
                throw new CodeException(ErrDefinition.E_DEVICE_IN_COMMOND);
            }
            byte[] contract_id=new byte[2];
            if(op.equals("register")){
                byte [] buf = imsi.getBytes();
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(buf);
                byte [] tmp = md5.digest();
                contract_id[0]=tmp[0];
                contract_id[1]=tmp[1];
            }
            else if(op.equals("unregister")){
                java.util.Arrays.fill(contract_id, (byte) 0);

            }else{
                throw new CodeException(ErrDefinition.E_COMMOND_REGISTER_INCORRECT_PARAM);
            }
            Commands commands =new Commands();
            commands.IMEI=imei;
            commands.command="CONTRACT";
            commands.contract=contract_id;
            commands.submit=new Date();
            commands.save();
            return success();
        }catch (CodeException ce){
            return failure(ce.getCode());
        }

    }
    public Result update(){
        DynamicForm form=formFactory.form().bindFromRequest();
        String imei  =form.get("IMEI");
        String firmware= form.get("firmware");

        try{

            if(firmware.isEmpty()||firmware==null){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Binary binary=Binary.finder.where().eq("name",firmware).findUnique();
            if(binary==null){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if(imei==null||imei.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMOND_REGISTER_INCORRECT_PARAM);
            }
            DeviceInfo deviceInfo=DeviceInfo.finder.where().eq("IMEI",imei).findUnique();
            if(!deviceInfo.commond.equals("ok")){
                throw new CodeException(ErrDefinition.E_DEVICE_IN_COMMOND);
            }
            Commands commands =new Commands();
            commands.IMEI=imei;
            commands.command="UPDATE";
            commands.str1=firmware;
            commands.binary_id=binary.id;
            commands.submit=new Date();
            commands.save();

            return success();
        }catch (CodeException ce){
            return failure(ce.getCode());
        }

    }
    public Result monitor(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String imei = form.get("IMEI");
            String duration = form.get("duration");
            String threshold = form.get("threshold");//每条记录包含帧数
            String interval = form.get("interval");
            String op=form.get("op");
            if (imei == null || imei.isEmpty()||op==null||op.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMOND_MONITOR_INCORRECT_PARAM);
            }
            Commands commands = new Commands();
            commands.command = "MONITOR";
            commands.submit=new Date();
            commands.IMEI=imei;
            DeviceInfo deviceInfo=DeviceInfo.finder.where().eq("IMEI",imei).findUnique();

            if(op.equals("close")){
                if(!deviceInfo.commond.equals("monitor")){
                    throw new CodeException(ErrDefinition.E_DEVICE_NOTIN_MONITOR);
                }
                commands.int1 = 0;
            }
            else if(op.equals("open")){
                if(!deviceInfo.commond.equals("ok")){
                    throw new CodeException(ErrDefinition.E_DEVICE_IN_COMMOND);
                }
                commands.int1 = session_id;
                session_id+=1;
            }else{
                throw new CodeException(ErrDefinition.E_COMMOND_MONITOR_INCORRECT_PARAM);
            }
            if(duration==null||interval==null||threshold==null){
                throw new CodeException(ErrDefinition.E_COMMOND_MONITOR_INCORRECT_PARAM);
            }
            commands.int2=Integer.parseInt(duration);
            commands.int3=Integer.parseInt(interval);
            commands.int4=Integer.parseInt(threshold);
            commands.save();
            return success();
        }
        catch (CodeException ce){
            return failure(ce.getCode());
        }
    }
}
