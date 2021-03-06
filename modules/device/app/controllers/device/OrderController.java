package controllers.device;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.account.Account;
import models.device.Devices;
import models.device.*;
import play.Logger;
import play.core.j.JavaResultExtractor;
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
    @Inject
    private Materializer mat;

    public Result gettopfive(){
        String sql="SELECT device_id,count(device_id) as type FROM ladder.`order` group by device_id order by count(device_id) desc limit 10 ";
        List<SqlRow> orderList=Ebean.createSqlQuery(sql).findList();
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
            Order order = Order.finder.where().eq("id",Integer.parseInt(id)).findUnique();
            if (order!=null){
                order.state="untreated";
                order.save();
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
            Order order = models.device.Order.finder.byId(Integer.parseInt(id));
			Dispatch dispatch = models.device.Dispatch.finder.where().eq("order_id",order.id).findUnique();
			if(order.state.equals("treating")){
			    throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}else{
			    order.state="treating";
			    order.save();
			    dispatch.state="treating";
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
                orderList = new ArrayList<>();
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

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);

            String imei = form.get("imei");
            if(imei !=null && !imei.isEmpty()){
                Devices devices = Devices.finder.where().eq("imei",imei).findUnique();
                if(devices!=null){
                    exprList.add(Expr.eq("device_id",devices.id));
                }
            }
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
            String item = form.get("item");
            if(item!=null&&!item.isEmpty()){
                exprList.add(Expr.eq("item",item));
            }
            String follow=form.get("follow");
            if(follow!=null&&!follow.isEmpty()){
                if(follow.equals("yes")){
                    List<Follow> followList= Follow.finder.where().eq("userId", session("userId")).findList();
                    Set<Integer> idlist=new HashSet<>();
                    for(Follow follows:followList){
                        idlist.add(follows.device_id);
                    }
                    exprList=exprList.in("device_id",idlist);
                    System.out.print(exprList);
                }
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
    public Result readDeviceName(){
        try {
            Result ret = read();
            int TIME_OUT = 10000;
            ByteString body = JavaResultExtractor.getBody(ret, TIME_OUT, mat);
            ObjectNode resultData = (ObjectNode) new ObjectMapper().readTree(body.decodeString("UTF-8"));
            if (resultData.get("code").asInt() != 0) {
                return ret;
            }
            int totalNum = resultData.get("data").get("totalNumber").asInt();

            int totalPage = resultData.get("data").get("totalPage").asInt();
            List<ObjectNode> nodeList = new ArrayList<>();
            for(JsonNode child : resultData.get("data").get("list")){
                ObjectNode node = (ObjectNode) new ObjectMapper().readTree(child.toString());
                DeviceInfo devices = DeviceInfo.finder.where().eq("id",node.get("device_id").asText()).findUnique();
                if(devices!=null){
                    node.put("device_name",devices.device_name);
                    nodeList.add(node);
                }
            }
            return successList(totalNum, totalPage, nodeList);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }

    public Result readUntreted(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Order> orderList;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Order Order = models.device.Order.finder.byId(Integer.parseInt(id));
                if (Order == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                orderList = new ArrayList<>();
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

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);

            String islast=form.get("islast");
            if (islast != null && !islast.isEmpty()) {
                exprList.add(Expr.eq("islast", Integer.parseInt(islast)));
            }
            String type=form.get("type");
            if (type != null && !type.isEmpty()) {
                exprList.add(Expr.eq("type", Integer.parseInt(type)));
            }
            String device_type=form.get("device_type");
            if (device_type != null && !device_type.isEmpty()) {
                exprList.add(Expr.eq("device_type", device_type));
            }
            String item = form.get("item");
            if(item!=null&&!item.isEmpty()){
                exprList.add(Expr.eq("item",item));
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
            exprList=exprList.not(Expr.eq("state", "treated"));

            orderList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("createTime desc")
                    .findList();


            int totalNum = Integer.parseInt(numStr);
            int totalPage = Integer.parseInt(pageStr);

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
    public Result readMore(){
        try {
            Result ret = readUntreted();
            int TIME_OUT = 10000;
            ByteString body = JavaResultExtractor.getBody(ret, TIME_OUT, mat);
            ObjectNode resultData = (ObjectNode) new ObjectMapper().readTree(body.decodeString("UTF-8"));
            if (resultData.get("code").asInt() != 0) {
                return ret;
            }
            List<ObjectNode> nodeList = new ArrayList<>();
            for(JsonNode child : resultData.get("data").get("list")){
                ObjectNode node = (ObjectNode) new ObjectMapper().readTree(child.toString());
                DeviceInfo devices = DeviceInfo.finder.where().eq("id",node.get("device_id").asText()).findUnique();
                if(devices!=null){
                    node.put("online",devices.state);
                    if(devices.state.equals("online")){
                        nodeList.add(node);
                    }
                }
            }
            int num = resultData.get("data").get("totalNumber").asInt();
            int totalNum = nodeList.size();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
            return successList(totalNum, totalPage, nodeList);
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
            int count = models.device.Order.finder.where()
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
            if(Order.state.equals("examined")){
                throw  new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }else{
				Order.state="treating";
                Order.save();
				dispatch.item=Order.item;
                dispatch.order_id=Order.id;
                dispatch.create_time=new Date().getTime()+"";
                dispatch.user_id=session("userId");
				dispatch.state="treating";
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
			ExpressionList<Order> exprList = Order.finder.where();
            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            if (null == starttime || starttime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == endtime || endtime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
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
			
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long st=sdf.parse(starttime).getTime();
            long ed= st+86400000;

            int monday =exprList
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int tuesday =exprList
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int wensday =exprList
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int thursday =exprList
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int friday =exprList
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int saturday =exprList
                    .ge("create_time", st)
                    .le("create_time", ed)
                    .findRowCount();
            st = ed;
            ed = ed+86400000;
            int sunday =exprList
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
		String item = form.get("item");
		if(starttime!=null&&!starttime.isEmpty()){
		    sql=sql+"AND time>'"+starttime+"' ";
		}
		if(endtime!=null&&!endtime.isEmpty()){
			sql=sql+"AND time>'"+endtime+"' ";
		}
		if(item!=null&&!item.isEmpty()){
			sql=sql+"AND item='"+item+"' ";
		}
		sql=sql+"group by code order by count(code) desc limit 10 ";
        List<SqlRow> orderList=Ebean.createSqlQuery(sql).findList();
        return successList(orderList);
    }
	
	public Result progress(){
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			String pageStr = form.get("page");
			String numStr = form.get("num");
			String stateStr = form.get("state");
			String item = form.get("item");
			if (null == pageStr || pageStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			
			if (null == numStr || numStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			int page = Integer.parseInt(pageStr);
			int num = Integer.parseInt(numStr);
			String sql="SELECT ladder.`order`.device_id,ladder.`order`.code,ladder.`order`.create_time,ladder.`dispatch`.expect_time,producer,ladder.`order`.state as state2,ladder.`dispatch`.state FROM ladder.`order` left join ladder.`dispatch` on ladder.`order`.id=ladder.`dispatch`.order_id ";
			if(stateStr ==null || stateStr.isEmpty()){
			    // sql=sql+"WHERE ladder.`order`.state<>'treated' or ladder.`dispatch`.state<>'treated' ";
			}
			if(stateStr.equals("6")){
			    // sql=sql+"WHERE ladder.`order`.state<>'treated' or ladder.`dispatch`.state<>'treated' ";
			}
			if(stateStr.equals("1")){
			    sql=sql+"WHERE ladder.`order`.state='examined' ";
			}
			if(stateStr.equals("2")){
			    sql=sql+"WHERE ladder.`order`.state='untreated' ";
			}
			if(stateStr.equals("3")){
			    sql=sql+"WHERE ladder.`dispatch`.state='untreated' ";
			}
			if(stateStr.equals("4")){
			    sql=sql+"WHERE ladder.`dispatch`.state='examined' ";
			}
			if(stateStr.equals("5")){
			    sql=sql+"WHERE ladder.`order`.state='treated' ";
			}
			if(item!=null&&!item.isEmpty()){
				sql=sql+"AND ladder.`order`.item='"+item+"' ";
			}
			sql=sql+"order by ladder.`order`.create_time desc limit "+(page-1)*num+","+num;
			List<SqlRow> orderList=Ebean.createSqlQuery(sql)
										.findList();
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
