package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.account.Account;
import models.device.Order;
import models.device.Dispatch;
import models.device.Follow;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import play.libs.Json;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lengxia on 2018/12/9.
 */
public class OrderController extends BaseController {

    @Inject
    FormFactory formFactory;

    public Result gettopfive(){
        String sql="SELECT device_id,count(device_id) as type FROM ladder.`order` group by device_id order by count(device_id) desc limit 10 ";
        List<SqlRow> orderList=Ebean.createSqlQuery(sql).findList();
        Logger.info(orderList.size()+"");
        return successList(orderList);
    }

    public Result examine(){
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
            Order order = models.device.Order.finder.byId(Integer.parseInt(id));
            order.state="examined";
            order.save();
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
            Order order = models.device.Order.finder.byId(Integer.parseInt(id));
            order.state="untreated";
            order.save();
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
            String islast=form.get("islast");
            if (islast != null && !islast.isEmpty()) {
                exprList.add(Expr.eq("islast", Integer.parseInt(islast)));
            }
            String type=form.get("type");
            if (type != null && !type.isEmpty()) {
                exprList.add(Expr.eq("type", Integer.parseInt(type)));
            }
            String producer=form.get("producer");
            if (producer != null && !producer.isEmpty()) {
                exprList.add(Expr.eq("producer", producer));
            }
            String device_type=form.get("device_type");
            if (device_type != null && !device_type.isEmpty()) {
                exprList.add(Expr.eq("device_type", device_type));
            }
            String state=form.get("state");
            if (state != null && !state.isEmpty()) {
                exprList.add(Expr.eq("state", state));
            }
            String starttime = form.get("starttime");
            if(starttime!=null&&!starttime.isEmpty()){
                exprList.add(Expr.ge("createTime",starttime));
            }
            String endtime = form.get("endtime");
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
            String mobile=form.get("mobile");
			String expect=form.get("expect_time");
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
            Dispatch dispatch =new Dispatch();
            if(mobile!=null&&!mobile.isEmpty()){
                dispatch.phone=mobile;
            }
            if(expect!=null&&!expect.isEmpty()){
                dispatch.expect_time=expect;
            }
            if(Order.state.equals("treated")){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }else{
                Order.state="treated";
                Order.save();
                dispatch.order_id=Order.id;
                dispatch.create_time=new Date().getTime()+"";
                dispatch.user_id=session("userId");
                dispatch.state="untreated";
                dispatch.device_id=Order.device_id;
                dispatch.order_type=Order.type;
				dispatch.code=Order.code;
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

    public Result readCountOrder(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Order> orderList = null;

            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            if (null == starttime || starttime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == endtime || endtime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long st=sdf.parse(starttime).getTime();
            long ed= st+86400000;

            int monday =Order.finder
                    .where()
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int tuesday =Order.finder
                    .where()
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int wensday =Order.finder
                    .where()
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int thursday =Order.finder
                    .where()
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int friday =Order.finder
                    .where()
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int saturday =Order.finder
                    .where()
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int sunday =Order.finder
                    .where()
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();

            ObjectNode node = Json.newObject();
            node.put("monday", String.format("%d", monday));
            node.put("tuesday", String.format("%d", tuesday));
            node.put("wensday", String.format("%d", wensday));
            node.put("thursday", String.format("%d", thursday));
            node.put("friday", String.format("%d", friday));
            node.put("saturday", String.format("%d", saturday));
            node.put("sunday", String.format("%d", sunday));

            return success("data",node);
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

	public Result faultfreq(){
		DynamicForm form = formFactory.form().bindFromRequest();
        String sql="SELECT code,count(1) as counter FROM ladder.`order` WHERE type=1 ";
		String starttime = form.get("starttime");
		String endtime = form.get("endtime");
		if(starttime!=null&&!starttime.isEmpty()){
		    sql=sql+"AND time>'"+starttime+"' ";
		}
		if(endtime!=null&&!endtime.isEmpty()){
			sql=sql+"AND time>'"+endtime+"' ";
		}
		sql=sql+"group by code order by count(code) desc limit 10 ";
        List<SqlRow> orderList=Ebean.createSqlQuery(sql).findList();
        Logger.info(orderList.size()+"");
        return successList(orderList);
    }
	
	public Result progress(){
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
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
			String sql="SELECT ladder.`order`.device_id,ladder.`order`.code,ladder.`order`.create_time,ladder.`dispatch`.expect_time,producer,ladder.`order`.state as state2,ladder.`dispatch`.state FROM ladder.`order` left join ladder.`dispatch` on ladder.`order`.id=ladder.`dispatch`.order_id WHERE ladder.`order`.state<>'treated' ";
			sql=sql+"order by ladder.`order`.create_time desc limit "+(page-1)*num+","+num;
			List<SqlRow> orderList=Ebean.createSqlQuery(sql)
										.findList();
			Logger.info(orderList.size()+"");
			return successList(orderList);
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
