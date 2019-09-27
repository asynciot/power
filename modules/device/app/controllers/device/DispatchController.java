package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.account.Account;
import models.device.DeviceInfo;
import models.device.Devices;
import models.device.Dispatch;
import models.device.Order;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by lengxia on 2018/12/9.
 */
public class DispatchController extends BaseController{
    @Inject
    FormFactory formFactory;

    public Result confirm(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String id = form.get("id");
            if(id==null||id.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Dispatch dispatch = models.device.Dispatch.finder.byId(Integer.parseInt(id));
            String order_path="./public/images/order/";
            File files=new File(order_path);
            if(!Files.exists(files.toPath())){
                Files.createDirectories(files.toPath());
            }
            StringBuilder confirmstr= new StringBuilder();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            if(body!=null){
                List<Http.MultipartFormData.FilePart> fileParts = body.getFiles();
                for(Http.MultipartFormData.FilePart filePart:fileParts){
                    File file=(File) filePart.getFile();
                    String filename=filePart.getFilename();
                    if(filename.startsWith("confirm")){
                        File storeFile = new File( order_path+ filename);
                        Files.move(file.toPath(),storeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        confirmstr.append("order/").append(filename).append(";");
                    }
                }
            }
            if (dispatch != null){
                dispatch.confirm_time=new Date().getTime()+"";
                dispatch.confirm_pic= confirmstr.toString();
                dispatch.save();
            }
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }

    public Result examine(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String id = form.get("id");
            String remarks=form.get("remarks");
            if(id==null||id.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String userId = session("userId");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Account adminAccount = Account.finder.byId(userId);
            if (adminAccount == null||adminAccount.augroup>1) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Dispatch dispatch = models.device.Dispatch.finder.byId(Integer.parseInt(id));
            if (dispatch!=null){
                dispatch.state="examined";
                if(remarks!=null){
                    dispatch.remarks=remarks;
                }
                dispatch.save();
            }
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }

    public Result adopt(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String id = form.get("id");
            if(id==null||id.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String userId = session("userId");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Account adminAccount = Account.finder.byId(userId);
            if (adminAccount == null||adminAccount.augroup>1) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Dispatch dispatch = models.device.Dispatch.finder.byId(Integer.parseInt(id));
            if (dispatch != null){
                dispatch.state="treated";
                dispatch.save();

                Order order=Order.finder.byId(dispatch.order_id);
                if (order !=null ){
                    order.state="treated";
                    if(!dispatch.result.equals("transfer")){
                        DeviceInfo deviceInfo=DeviceInfo.finder.byId(dispatch.device_id);
                        if(deviceInfo==null){
                            throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                        }
                        if(dispatch.order_type==2){
                            deviceInfo.maintenance_lasttime=new Date().getTime()+"";
                        }
                        if(dispatch.order_type==3){
                            deviceInfo.inspection_lasttime=new Date().getTime()+"";

                        }
                        String inspection_nexttime=form.get("inspection_nexttime");
                        if(inspection_nexttime!=null&&!inspection_nexttime.isEmpty()){
                            deviceInfo.inspection_nexttime=inspection_nexttime;
                        }
                        String maintenance_nexttime=form.get("maintenance_nexttime");
                        if(maintenance_nexttime!=null&&!maintenance_nexttime.isEmpty()){
                            deviceInfo.maintenance_nexttime=maintenance_nexttime;
                        }
                        deviceInfo.save();

                    }else {
                        order.state="untreated";
                    }
                    order.save();
                }
            }
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }

    public Result handover(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String to_user = form.get("to_user");
            if(to_user==null||to_user.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Account account=Account.finder.where().eq("username",to_user).findUnique();
            if(account==null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            List<Dispatch> dispatchList = models.device.Dispatch.finder.where().eq("user_id",session("userId")).ieq("state","finish").findList();
            for(Dispatch dispatch:dispatchList){
                dispatch.user_id=account.id;
            }
            Ebean.saveAll(dispatchList);
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
    public Result finish(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String id = form.get("id");
            String result=form.get("result");
            String remarks=form.get("remarks");
            Logger.info(result+"ss");
            if(id==null||id.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if(result==null||result.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Dispatch dispatch = models.device.Dispatch.finder.byId(Integer.parseInt(id));
//            if(dispatch.state!="examined"){
//                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
//            }
            String order_path="./public/images/order/";
            File files=new File(order_path);
            if(!Files.exists(files.toPath())){
                Files.createDirectories(files.toPath());
            }
            StringBuilder beforestr= new StringBuilder();
            StringBuilder afterstr= new StringBuilder();
            Http.MultipartFormData body = request().body().asMultipartFormData();
            if(body!=null){
                List<Http.MultipartFormData.FilePart> fileParts = body.getFiles();
                for(Http.MultipartFormData.FilePart filePart:fileParts){
                    File file=(File) filePart.getFile();
                    String filename=filePart.getFilename();
                    File storeFile = new File( order_path+ filename);
                    Files.move(file.toPath(),storeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if(filename.startsWith("before")){
                        beforestr.append("order/").append(filename).append(";");
                    }
                    if(filename.startsWith("after")){
                        afterstr.append("order/").append(filename).append(";");
                    }
                }
            }
            if (dispatch!=null){
                if(remarks!=null){
                    dispatch.remarks=remarks;
                }
                dispatch.result=result;
                dispatch.state="examined";
                dispatch.finish_time=new Date().getTime()+"";
                dispatch.before_pic= beforestr.toString();
                dispatch.after_pic= afterstr.toString();
                dispatch.save();
            }
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
    public Result read() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Dispatch> dispatchList;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Dispatch fault = Dispatch.finder.byId(Integer.parseInt(id));
                if (fault == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                dispatchList = new ArrayList<>();
                dispatchList.add(fault);
                return successList(1, 1, dispatchList);
            }

            ExpressionList<Dispatch> exprList = Dispatch.finder.where();
            String pageStr = form.get("page");
            String numStr = form.get("num");
            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);

            String imei = form.get("imei");
            if(imei != null && !imei.isEmpty()){
                Devices devices = Devices.finder.where().eq("imei",imei).findUnique();
                if (devices!=null){
                    exprList.add(Expr.eq("device_id",devices.id));
                }
            }
            String order_id = form.get("order_id");
            if (order_id != null && !order_id.isEmpty()) {
                exprList.add(Expr.eq("order_id", Integer.parseInt(order_id)));
            }
            String device_id = form.get("device_id");
            if (device_id != null && !device_id.isEmpty()) {
                exprList.add(Expr.eq("device_id", Integer.parseInt(device_id)));
            }
            String order_type = form.get("order_type");
            if (order_type != null && !order_type.isEmpty()) {
                exprList.add(Expr.eq("order_type", order_type));
            }
            String state = form.get("state");
            if (state != null && !state.isEmpty()) {
                exprList.add(Expr.eq("state", state));
            }
            String user_id = form.get("user_id");
            if (user_id != null && !user_id.isEmpty()) {
                exprList.add(Expr.eq("user_id", user_id));
            }
            String follow = form.get("follow");
            if (follow != null && !follow.isEmpty() && follow.equals("yes")) {
                exprList.add(Expr.eq("user_id", session("userId")));
            }
            String starttime = form.get("creat_starttime");
            String endtime = form.get("create_endtime");
            if (starttime != null && !starttime.isEmpty()) {
                exprList.add(Expr.ge("create_time", starttime));
            }
            if (endtime != null && !endtime.isEmpty()) {
                exprList.add(Expr.le("create_time", endtime));
            }
            starttime = form.get("finish_starttime");
            endtime = form.get("finish_endtime");
            if (starttime != null && !starttime.isEmpty()) {
                exprList.add(Expr.ge("finish_time", starttime));
            }
            if (endtime != null && !endtime.isEmpty()) {
                exprList.add(Expr.le("finish_time", endtime));
            }
            String item = form.get("item");
            if(item!=null&&!item.isEmpty()){
                exprList.add(Expr.eq("item",item));
            }
            dispatchList = exprList
                    .setFirstRow((page - 1) * num)
                    .setMaxRows(num)
                    .orderBy("create_time desc")
                    .findList();
            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, dispatchList);
        } catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
}
