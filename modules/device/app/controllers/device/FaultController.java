package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import models.device.Fault;
import models.device.Repair;
import models.device.Follow;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lengxia on 2018/12/9.
 */
public class FaultController extends XDomainController {

    @Inject
    FormFactory formFactory;
    public Result read(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Fault> faultList = null;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Fault fault = Fault.finder.byId(Integer.parseInt(id));
                if (fault == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                faultList = new ArrayList<Fault>();
                faultList.add(fault);
                return successList(1, 1, faultList);
            }

            ExpressionList<Fault> exprList = Fault.finder.where();
            String pageStr = form.get("page");
            String numStr = form.get("num");
            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);

            String device_id=form.get("device_id");
            if (device_id != null && !device_id.isEmpty()) {
                exprList.add(Expr.eq("device_id", device_id));
            }
            String type=form.get("type");
            if (type != null && !type.isEmpty()) {
                exprList.add(Expr.eq("type", type));
            }
            String state=form.get("state");
            if (state != null && !state.isEmpty()) {
                exprList.add(Expr.eq("state", state));
            }
            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            if(starttime!=null&&!starttime.isEmpty()){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dates = sdf.parse(starttime);
                exprList.add(Expr.ge("createTime",dates));
            }
            if(endtime!=null&&!endtime.isEmpty()){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dates = sdf.parse(endtime);
                exprList.add(Expr.le("createTime",dates));
            }

            String follow=form.get("follow");
            if(follow!=null&&!follow.isEmpty()&&follow.equals("yes")){
                List<Follow> followList= Follow.finder.where().eq("userId", session("userId")).findList();
                Set<Integer> idlist=new HashSet<>();
                for(Follow follows:followList){
                        idlist.add(follows.device_id);
                }
                exprList=exprList.in("device_id",idlist);
            }

            faultList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("createTime desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, faultList);
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

    public Result create() {
        try {
            Form<Fault> form = formFactory.form(Fault.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            Fault fault = form.get();
            if(fault.device_id==null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            fault.createTime=new Date();
            int count = Fault.finder.where()
                    .eq("device_id", fault.device_id)
                    .eq("type", fault.type)
                    .eq("state",fault.state)
                    .findRowCount();

            if (count != 0) {
                throw new CodeException(ErrDefinition.E_COMMOND_REGISTER_ALREADY_EXIST);
            }
            Ebean.save(fault);
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FOLLOW_INFO_CREATE_FAILED);

        }
    }
    public Result order(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String fault_id=form.get("fault_id");
            if(fault_id==null||fault_id.isEmpty()){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String userId= session("userId");
            if(userId==null||userId.isEmpty()){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Fault fault= Fault.finder.byId(Integer.parseInt(fault_id));
            if(fault==null){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            fault.state="treated";
            fault.save();

            Repair repair=new Repair();
            repair.device_id=fault.device_id;
            repair.fault_id=fault_id;
            repair.create_time=new Date();
            repair.state="untreated";
            repair.userId=session("userId");
            repair.save();
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

}
