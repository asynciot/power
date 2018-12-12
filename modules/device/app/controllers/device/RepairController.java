package controllers.device;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Repair;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by lengxia on 2018/12/9.
 */
public class RepairController extends BaseController{
    @Inject
    FormFactory formFactory;

    public Result finish(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String id = form.get("id");
            if(id==null||id.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Repair repair=Repair.finder.byId(Integer.parseInt(id));
            repair.state="treated";
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
    public Result read(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Repair> repairList = null;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Repair fault = Repair.finder.byId(Integer.parseInt(id));
                if (fault == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                repairList = new ArrayList<Repair>();
                repairList.add(fault);
                return successList(1, 1, repairList);
            }

            ExpressionList<Repair> exprList = Repair.finder.where();
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

            String fault_id=form.get("fault_id");
            if (fault_id != null && !fault_id.isEmpty()) {
                exprList.add(Expr.eq("fault_id", fault_id));
            }
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
            String userId=form.get("userId");
            if (userId != null && !userId.isEmpty()) {
                exprList.add(Expr.eq("userId", userId));
            }
            String follow=form.get("follow");
            if(follow!=null&&!follow.isEmpty()&&follow.equals("yes")){
                exprList.add(Expr.eq("userId",session("userId")));
            }
            repairList = exprList
                .setFirstRow((page-1)*num)
                .setMaxRows(num)
                .orderBy("create_time desc")
                .findList();
            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, repairList);
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
