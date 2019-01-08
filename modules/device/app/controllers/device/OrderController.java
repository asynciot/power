package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import models.device.Order;
import models.device.Dispatch;
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
public class OrderController extends BaseController {

    @Inject
    FormFactory formFactory;

    public Result read(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Order> orderList = null;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Order Order = models.device.Order.finder.byId(Integer.parseInt(id));
                if (Order == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                orderList = new ArrayList<Order>();
                orderList.add(Order);
                return successList(1, 1, orderList);
            }

            ExpressionList<Order> exprList = Order.finder.where();
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
                exprList.add(Expr.eq("device_id", Integer.parseInt(device_id)));
            }
            String type=form.get("type");
            if (type != null && !type.isEmpty()) {
                exprList.add(Expr.eq("type", Integer.parseInt(type)));
            }
            String producer=form.get("producer");
            if (producer != null && !producer.isEmpty()) {
                exprList.add(Expr.eq("producer", producer));
            }
            String state=form.get("state");
            if (state != null && !state.isEmpty()) {
                exprList.add(Expr.eq("state", state));
            }
            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            if(starttime!=null&&!starttime.isEmpty()){
                exprList.add(Expr.ge("createTime",starttime));
            }
            if(endtime!=null&&!endtime.isEmpty()){
                exprList.add(Expr.le("createTime",endtime));
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

            orderList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("createTime desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, orderList);
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
    public Result delete(){
        return delete(Order.class,formFactory);
    }

    public Result create() {
        try {
            Form<Order> form = formFactory.form(Order.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            Order Order = form.get();
            if(Order.device_id==null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Order.createTime=new Date().getTime()+"";
            int count = Order.finder.where()
                    .eq("device_id", Order.device_id)
                    .eq("type", Order.type)
                    .eq("state", Order.state)
                    .findRowCount();

            if (count != 0) {
                throw new CodeException(ErrDefinition.E_COMMOND_REGISTER_ALREADY_EXIST);
            }
            Ebean.save(Order);
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
    public Result receipt(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            String order_id=form.get("order_id");
            if(order_id==null||order_id.isEmpty()){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String userId= session("userId");
            if(userId==null||userId.isEmpty()){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Order Order = models.device.Order.finder.byId(Integer.parseInt(order_id));
            if(Order ==null){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Order.state="treating";
            Order.save();
            Dispatch dispatch =new Dispatch();
            dispatch.order_id=Order.id;
            dispatch.create_time=new Date().getTime()+"";
            dispatch.user_id=session("userId");
            dispatch.state="untreated";
            dispatch.device_id=Order.device_id;
            dispatch.order_type=Order.type;
            dispatch.save();
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