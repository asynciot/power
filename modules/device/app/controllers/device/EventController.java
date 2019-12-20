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
import models.device.Devices;
import models.device.Events;
import models.device.Ladder;
import models.device.SimplifyEvents;
import play.Logger;
import play.core.j.JavaResultExtractor;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;
import play.libs.Json;
import scala.util.Try;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2018/10/30.
 */
public class EventController extends BaseController {
    @Inject
    private FormFactory formFactory;
    @Inject
    private Materializer mat;

    private static int TIME_OUT = 10000;

    public Result create() {
        return create(Events.class, formFactory);
    }
    public Result read(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Events> eventsList;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Events events = Events.finder.byId(Integer.parseInt(id));
                if (events == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                eventsList = new ArrayList<>();
                eventsList.add(events);
                return successList(1, 1, eventsList);
            }
            String device_id=form.get("device_id");
            ExpressionList<Events> exprList = Events.finder.where().eq("device_id",device_id);
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

            String startTime = form.get("starttime");
            String endTime = form.get("endtime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(startTime!=null&&!startTime.isEmpty()){
                Date st = sdf.parse(startTime);
                exprList.add(Expr.ge("time",st));
            }
            if(endTime!=null&&!endTime.isEmpty()){
                Date ed = sdf.parse(endTime);
                exprList.add(Expr.le("time",ed));
            }
            String length=form.get("length");
            if (length != null && !length.isEmpty()) {
                exprList.add(Expr.eq("length", length));
            }
            String interval=form.get("interval");
            if (interval != null && !interval.isEmpty()) {
                exprList.add(Expr.eq("interval", interval));
            }

            eventsList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("time desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, eventsList);
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
    public Result readByIMEI(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Events> eventsList;
            ExpressionList<Events> exprList = null;
            String imei = form.get("imei");
            if(imei!=null && !imei.isEmpty()){
                Devices devices = Devices.finder.where().eq("imei",imei).findUnique();
                exprList = Events.finder.where().eq("device_id",devices.id);
            }
            if(exprList==null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String startTime = form.get("startTime");
            String endTime = form.get("endTime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(startTime!=null&&!startTime.isEmpty()){
                Date st = sdf.parse(startTime);
                exprList.add(Expr.ge("time",st));
            }
            if(endTime!=null&&!endTime.isEmpty()){
                Date ed = sdf.parse(endTime);
                exprList.add(Expr.le("time",ed));
            }
            String length=form.get("length");
            if (length != null && !length.isEmpty()) {
                exprList.add(Expr.eq("length", length));
            }
            String interval=form.get("interval");
            if (interval != null && !interval.isEmpty()) {
                exprList.add(Expr.eq("interval", interval));
            }
            eventsList = exprList
                    .orderBy("time desc")
                    .findList();
            int totalNum = exprList.findRowCount();
            int totalPage = 1;
            return successList(totalNum, totalPage, eventsList);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
    public Result readEventNoPage(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Events> eventsList;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Events events = Events.finder.byId(Integer.parseInt(id));
                if (events == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                eventsList = new ArrayList<>();
                eventsList.add(events);
                return successList(1, 1, eventsList);
            }
            String device_id=form.get("device_id");
            String imei=form.get("imei");
            ExpressionList<Events> exprList = null;
            if(device_id!=null&&!device_id.isEmpty()){
                exprList = Events.finder.where().eq("device_id",device_id);
            }
            if (imei!=null&&!imei.isEmpty()){
                exprList = Events.finder.where().eq("imei",imei);
            }
            if(exprList==null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String startTime = form.get("startTime");
            String endTime = form.get("endTime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(startTime!=null&&!startTime.isEmpty()){
                Date st = sdf.parse(startTime);
                exprList.add(Expr.ge("time",st));
            }
            if(endTime!=null&&!endTime.isEmpty()){
                Date ed = sdf.parse(endTime);
                exprList.add(Expr.le("time",ed));
            }
            String length=form.get("length");
            if (length != null && !length.isEmpty()) {
                exprList.add(Expr.eq("length", length));
            }
            String interval=form.get("interval");
            if (interval != null && !interval.isEmpty()) {
                exprList.add(Expr.eq("interval", interval));
            }
            eventsList = exprList
                    .orderBy("time desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = 1;

            return successList(totalNum, totalPage, eventsList);
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
    public Result readCountEvent(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Events> eventsList = null;

            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            if (null == starttime || starttime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == endtime || endtime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long strt = sdf.parse(starttime).getTime();
            long edt= strt+86400000;
            Date st = sdf.parse(sdf.format(strt));
            Date ed = sdf.parse(sdf.format(edt));

            int monday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int tuesday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int wensday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int thursday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int friday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int saturday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int sunday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
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

	public Result activedoor(){
		DynamicForm form = formFactory.form().bindFromRequest();
	    String sql="SELECT device_id,device_name,count(1) as counter FROM ladder.simplify_events inner join ladder.`device_info` on ladder.`simplify_events`.device_id=ladder.`device_info`.id WHERE ladder.`events`.device_id>0 ";
		String startTime = form.get("starttime");
		String endTime = form.get("endtime");
		String item = form.get("item");
		if(startTime!=null&&!startTime.isEmpty()){
		    sql=sql+"AND time>'"+startTime+"' ";
		}
		if(endTime!=null&&!endTime.isEmpty()){
				sql=sql+"AND time>'"+endTime+"' ";
		}
		if(item!=null&&!item.isEmpty()){
			sql=sql+"AND item='"+item+"' ";
		}
		sql=sql+"group by device_id order by count(device_id) desc limit 10";
	    List<SqlRow> orderList=Ebean.createSqlQuery(sql).findList();

	    return successList(orderList);
	}

    public Result ladderEvents(){
        DynamicForm form = formFactory.form().bindFromRequest();
        String sql="SELECT device_id,device_name,count(1) as counter FROM ladder.simplify_events inner join ladder.`device_info` on ladder.simplify_events.device_id=ladder.`device_info`.id WHERE ladder.simplify_events.device_id>0 And device_type='15' ";
        String startTime = form.get("startTime");
        String endTime = form.get("endTime");
        String item = form.get("item");
        if(startTime!=null&&!startTime.isEmpty()){
            sql=sql+"AND start_time>'"+startTime+"' ";
        }
        if(endTime!=null&&!endTime.isEmpty()){
            sql=sql+"AND end_time>'"+endTime+"' ";
        }
        if(item!=null&&!item.isEmpty()){
            sql=sql+"AND item='"+item+"' ";
        }
        sql=sql+"group by device_id order by count(device_id) desc limit 10";
        List<SqlRow> orderList=Ebean.createSqlQuery(sql).findList();
        return successList(startTime!=null ?Integer.parseInt(startTime):0,endTime!=null ?Integer.parseInt(endTime):0,orderList);
    }

//    public Result ReadMore(){
//        try{
//            Result ret = ladderEvents();
//            ByteString body = JavaResultExtractor.getBody(ret, TIME_OUT, mat);
//            ObjectNode resultData = (ObjectNode) new ObjectMapper().readTree(body.decodeString("UTF-8"));
//            if (resultData.get("code").asInt() != 0) {
//                return ret;
//            }
//            int startTime = resultData.get("data").get("startTime").asInt();
//            int endTime = resultData.get("data").get("endTime").asInt();
//            List<ObjectNode> nodeList = new ArrayList<>();
//            for (JsonNode child : resultData.get("data").get("list")) {
//                ObjectNode node = (ObjectNode) new ObjectMapper().readTree(child.toString());
//                Devices devices = Devices.finder.where().eq("id", node.get("device_id")).findUnique();
//                Ladder ladder = Ladder.finder.where().eq("door1",devices.IMEI).findUnique();
//                List<SimplifyEvents> Simple1 = new ArrayList<>();
//                List<SimplifyEvents> Simple2 = new ArrayList<>();
//                if (ladder.ctrl!=null){
//                    Simple1 = SimplifyEvents.finder.where().eq("device_id",ladder.ctrl).le("end_time",endTime).ge("start_time",startTime).findList();
//                    node.putPOJO("ctrlList",Simple1);
//                }
//
//
//            }
//            return successList(nodeList);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public Result update() {
        return update(Events.class, formFactory);
    }
    public Result delete() {
        return delete(Events.class, formFactory);
    }
    public Result readSimpleEvents(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<SimplifyEvents> eventsList;
            ExpressionList<SimplifyEvents> exprList = null;
            String pageStr = form.get("page");
            String numStr = form.get("num");
            String imei = form.get("imei");
            if(imei!=null && !imei.isEmpty()){
                Devices devices = Devices.finder.where().eq("imei",imei).findUnique();
                if (devices==null){
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                exprList = SimplifyEvents.finder.where().eq("device_id",devices.id);
            }
            String device_id = form.get("device_id");
            if (device_id!=null && !device_id.isEmpty()){
                exprList = SimplifyEvents.finder.where().eq("device_id",device_id);
            }
            if(exprList==null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String startTime = form.get("startTime");
            String endTime = form.get("endTime");
            Logger.info(endTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(startTime!=null&&!startTime.isEmpty()){
                exprList.add(Expr.ge("start_time",startTime));
            }
            if(endTime!=null&&!endTime.isEmpty()){
                exprList.add(Expr.le("end_time",endTime));
            }
            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);
            eventsList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("start_time desc")
                    .findList();
            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
            return successList(totalNum, totalPage, eventsList);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
    public Result readSimple(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<SimplifyEvents> eventsList;
            ExpressionList<SimplifyEvents> exprList = null;
            String imei = form.get("imei");
            if(imei!=null && !imei.isEmpty()){
                Devices devices = Devices.finder.where().eq("imei",imei).findUnique();
                if (devices==null){
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                exprList = SimplifyEvents.finder.where().eq("device_id",devices.id);
            }
            String device_id = form.get("device_id");
            if (device_id!=null && !device_id.isEmpty()){
                exprList = SimplifyEvents.finder.where().eq("device_id",device_id);
            }
            if(exprList==null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            String startTime = form.get("startTime");
            String endTime = form.get("endTime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(startTime!=null&&!startTime.isEmpty()){
                Date st = sdf.parse(startTime);
                exprList.add(Expr.ge("start_time",st));
            }
            if(endTime!=null&&!endTime.isEmpty()){
                Date ed = sdf.parse(endTime);
                exprList.add(Expr.le("end_time",ed));
            }
            eventsList = exprList
                    .orderBy("start_time desc")
                    .findList();
            int totalNum = exprList.findRowCount();
            int totalPage = 1;
            return successList(totalNum, totalPage, eventsList);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
    public Result readLadderEvent(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<SimplifyEvents> eventsList;
            ExpressionList<SimplifyEvents> exprList = SimplifyEvents.finder.where();
            String id1 = form.get("id1");
            String id2 = form.get("id2");
            String start_time = form.get("startTime");
            String end_time = form.get("endTime");
            if(id1!=null&&!id1.isEmpty()&&id2!=null&&!id2.isEmpty()) {
                exprList.add(Expr.ge("start_time",start_time));
                exprList.add(Expr.le("end_time",end_time));
                exprList.or(Expr.eq("device_id",id1),Expr.eq("device_id",id2));
            }else if (id1!=null && !id1.isEmpty()){
                exprList.add(Expr.ge("start_time",start_time));
                exprList.add(Expr.le("end_time",end_time));
                exprList.add(Expr.eq("device_id",id1));
            }else if (id2!=null&& !id2.isEmpty()){
                exprList.add(Expr.ge("start_time",start_time));
                exprList.add(Expr.le("end_time",end_time));
                exprList.add(Expr.eq("device_id",id2));
            }
            eventsList = exprList.orderBy("id").findList();
            int num = eventsList.size();
            return successList(num, 1, eventsList);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
    public Result readEventMore(){
        try{
            Result ret = readLadderEvent();
            int TIME_OUT = 10000;
            ByteString body = JavaResultExtractor.getBody(ret, TIME_OUT, mat);
            ObjectNode resultData = (ObjectNode) new ObjectMapper().readTree(body.decodeString("UTF-8"));
            if (resultData.get("code").asInt() != 0) {
                return ret;
            }
            int totalNum = resultData.get("data").get("totalNumber").asInt();
            int totalPage = resultData.get("data").get("totalPage").asInt();
            List<ObjectNode> nodeList = new ArrayList<>();
            for (JsonNode child : resultData.get("data").get("list")) {
                ObjectNode node = (ObjectNode) new ObjectMapper().readTree(child.toString());
                Devices devices = Devices.finder.where().eq("id",node.get("device_id").asText()).findUnique();
                node.put("device_model",devices.model);
                nodeList.add(node);
            }
            return successList(totalNum, totalPage, nodeList);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
}
