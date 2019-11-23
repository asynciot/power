package controllers.device;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;

import models.device.*;

import play.Logger;
import play.core.j.JavaResultExtractor;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.ws.WSClient;
import play.mvc.Result;
import sun.rmi.runtime.Log;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lengxia on 2018/10/30.
 */
public class LadderController extends BaseController {
    @Inject
    private FormFactory formFactory;

    @Inject
    private Materializer mat;

    private static int TIME_OUT = 10000;

    public Result create() {
        try {
            Form<Ladder> form = formFactory.form(Ladder.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
            }
            Ladder ladderInfo = form.get();
            if (ladderInfo.name == null || ladderInfo.name.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
            }
            Devices devices;
            if (!ladderInfo.ctrl.isEmpty()) {
                devices = Devices.finder.where().eq("IMEI", ladderInfo.ctrl).findUnique();
                if (devices!=null){
                    ladderInfo.ctrl_id = devices.id.toString();
                }
            }else{
                devices = Devices.finder.where().eq("IMEI", ladderInfo.door1).findUnique();
                if (devices!=null){
                    ladderInfo.ctrl_id = devices.id.toString();
                }
            }
            DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("IMEI", devices.IMEI).findUnique();
            if(deviceInfo!=null){
                ladderInfo.state = deviceInfo.state;
                Ebean.save(ladderInfo);
                deviceInfo.ladder_id = ladderInfo.id.toString();
                Ebean.save(deviceInfo);
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
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<Ladder> exprList= Ladder.finder.where();
            List<Ladder> ladderInfoList = new ArrayList<>();

            String search_info = form.get("search_info");
            String ladder_id = form.get("id");
            String group_id = form.get("group_id");
            String register = form.get("register");
            String tag = form.get("tagcolor");
            String state = form.get("state");
            String pageStr = form.get("page");
            String numStr = form.get("num");
            String follow = form.get("follow");
            String install_addr = form.get("install_addr");

            if (ladder_id != null && !ladder_id.isEmpty()) {
                Ladder ladder = Ladder.finder.byId(Integer.parseInt(ladder_id));
                if(ladder!=null){
                    ladderInfoList.add(ladder);
                }
                return successList(ladderInfoList.size(), 1, ladderInfoList);
            }
            if (search_info != null && !search_info.isEmpty()){
                exprList=Ladder.finder.where().contains("ctrl",search_info);
                if(exprList.findRowCount()<1) {
                    exprList = Ladder.finder.where().contains("door1", search_info);
                    if (exprList.findRowCount() < 1) {
                        exprList = Ladder.finder.where().contains("door2", search_info);
                        if (exprList.findRowCount() < 1) {
                            exprList = Ladder.finder.where().contains("name", search_info);
                        }
                    }
                }
            }
            if(follow!=null&&!follow.isEmpty()&&follow.equals("yes")){
                List<FollowLadder> followList= FollowLadder.finder.where().eq("user_id", session("userId")).findList();
                Set<String> ctrllist=new HashSet<>();
                for(FollowLadder follows:followList){
                    ctrllist.add(follows.ctrl);
                }
                exprList=exprList.in("ctrl",ctrllist);
            }
            if (group_id != null && !group_id.isEmpty()) {
                exprList=exprList.in("group_id",group_id);
            }
            if (state != null && !state.isEmpty()) {
                exprList=exprList.contains("state",state);
            }
            if (tag != null && !tag.isEmpty()) {
                exprList=exprList.contains("tagcolor",tag);
            }
            if(register!=null&&!register.isEmpty()){
                exprList=exprList.eq("register",register);
            }
            if(install_addr!=null&&!install_addr.isEmpty()){
                exprList=exprList.contains("install_addr",install_addr);
            }
            String item=form.get("item");
            if(item!=null&&!item.isEmpty()){
                exprList=exprList.contains("item",item);
            }
            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);

            ladderInfoList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, ladderInfoList);
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
            Result ret = read();
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
                if(!node.get("ctrl").asText().equals("")){
                    DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei", node.get("ctrl").asText()).findUnique();
                    Devices devices = Devices.finder.where().eq("imei", node.get("ctrl").asText()).findUnique();
                    Devices door1 = Devices.finder.where().eq("imei", node.get("door1").asText()).findUnique();

                    node.put("door_id1",door1.id);
                    if (deviceInfo != null) {
                        node.put("rssi", deviceInfo.rssi);
                        node.put("tagcolor", deviceInfo.tagcolor);
                        node.put("cellocation_id", deviceInfo.cellocation_id);
                        node.put("IPlocation_id", deviceInfo.IPlocation_id);
                    }
                    Cellocation cellocation = Cellocation.finder.where().eq("id", deviceInfo.cellocation_id).findUnique();
                    IPlocation iplocation = IPlocation.finder.where().eq("id", deviceInfo.IPlocation_id).findUnique();
                    if (cellocation != null) {
                        node.put("cell_lat", cellocation.lat);
                        node.put("cell_lon", cellocation.lon);
                        node.put("cell_radius", cellocation.radius);
                        node.put("cell_address", cellocation.address);
                    }
                    if (iplocation != null) {
                        node.put("ip_ip", iplocation.ip);
                        node.put("ip_city", iplocation.city);
                        node.put("ip_country", iplocation.country);
                        node.put("ip_region", iplocation.region);
                    }
                    if(devices !=null){
                        if(devices.t_logout!=null){
                            node.put("t_logout", devices.t_logout.toString());
                        }
                        if(devices.t_update!=null){
                            node.put("t_update", devices.t_update.toString());
                        }
                    }
                }else {
                    DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei", node.get("door1").asText()).findUnique();
                    Devices devices = Devices.finder.where().eq("imei", node.get("door1").asText()).findUnique();
                    if (deviceInfo != null) {
                        node.put("rssi", deviceInfo.rssi);
                        node.put("tagcolor", deviceInfo.tagcolor);
                        node.put("cellocation_id", deviceInfo.cellocation_id);
                        node.put("IPlocation_id", deviceInfo.IPlocation_id);
                    }
                    Cellocation cellocation = Cellocation.finder.where().eq("id", deviceInfo.cellocation_id).findUnique();
                    IPlocation iplocation = IPlocation.finder.where().eq("id", deviceInfo.IPlocation_id).findUnique();
                    if (cellocation != null) {
                        node.put("cell_lat", cellocation.lat);
                        node.put("cell_lon", cellocation.lon);
                        node.put("cell_radius", cellocation.radius);
                        node.put("cell_address", cellocation.address);
                    }
                    if (iplocation != null) {
                        node.put("ip_ip", iplocation.ip);
                        node.put("ip_city", iplocation.city);
                        node.put("ip_country", iplocation.country);
                        node.put("ip_region", iplocation.region);
                    }
                    if(devices !=null){
                        if(devices.t_logout!=null){
                            node.put("t_logout", devices.t_logout.toString());
                        }
                        if(devices.t_update!=null){
                            node.put("t_update", devices.t_update.toString());
                        }
                    }
                }
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

    public Result ReadFault(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Ladder> ladderList = Ladder.finder.where().eq("state","online").findList();
            ExpressionList<Order> exprList = Order.finder.where();

            String numStr = form.get("num");
            List<FollowLadder> followList= FollowLadder.finder.where().eq("user_id", session("userId")).findList();
            Set<String> ctrllist=new HashSet<>();
            for(FollowLadder follows:followList){
                ctrllist.add(String.valueOf(follows.ladder_id));
            }
            exprList=exprList.in("id",ctrllist);
            for(Ladder ladder: ladderList){
                if(ladder.state.equals("online")){
                    exprList.add(Expr.eq("device_id", ladder.ctrl_id));
                    exprList.add(Expr.eq("islast", 1));
                }
            }
            exprList = exprList.not(Expr.eq("state", "treated"));
            List<Order> orderList = exprList.findList();

            int num = Integer.parseInt(numStr);
            int totalNum = exprList.findRowCount();

            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum,totalPage,orderList);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }

    public Result update() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String ladder_id = form.get("ladder_id");
            String name = form.get("name");
            String install_addr = form.get("install_addr");
            String type = form.get("type");
            String IMEI = form.get("IMEI");

            if (ladder_id == null || ladder_id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Ladder ladder=Ladder.finder.byId(Integer.parseInt(ladder_id));
            if(ladder == null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if(name!=null&&!name.isEmpty()){
                ladder.name=name;
            }
            if(install_addr!=null&&!install_addr.isEmpty()){
                ladder.install_addr=install_addr;
            }
            DeviceInfo deviceInfo = new DeviceInfo();
            if(type!=null&&!type.isEmpty()){
                switch (type) {
                    case "1":
                        ladder.ctrl = IMEI;
                        deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.ctrl).findUnique();
                        if (deviceInfo != null) {
                            deviceInfo.ladder_id = ladder_id;
                            ladder.ctrl_id = deviceInfo.id.toString();
                        }
                        break;
                    case "2":
                        ladder.door1 = IMEI;
                        deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.door1).findUnique();
                        if (deviceInfo != null) {
                            deviceInfo.ladder_id = ladder_id;
                            ladder.ctrl_id = deviceInfo.id.toString();
                        }
                        break;
                    case "3":
                        ladder.door2 = IMEI;
                        deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.door2).findUnique();
                        if (deviceInfo != null) {
                            deviceInfo.ladder_id = ladder_id;
                            ladder.ctrl_id = deviceInfo.id.toString();
                        }
                        break;
                }
            }
            Ebean.save(deviceInfo);
            ladder.save();
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

    public Result group() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String ladder_id = form.get("ladder_id");
            String id = form.get("id");
            String imei = form.get("imei");
            if (ladder_id == null || ladder_id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Ladder ladder=Ladder.finder.byId(Integer.parseInt(ladder_id));
            DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei",imei).findUnique();
            if(ladder !=null){
                ladder.group_id = id;
                ladder.save();
            }
            if(deviceInfo != null){
                deviceInfo.group_id = id;
                deviceInfo.save();
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

    public Result rmGroup() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String ladder_id = form.get("ladder_id");
            String imei = form.get("imei");

            if (ladder_id == null || ladder_id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Ladder ladder=Ladder.finder.byId(Integer.parseInt(ladder_id));
            DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei",imei).findUnique();
            if(ladder !=null){
                ladder.group_id = null;
                ladder.save();
            }
            if(deviceInfo != null){
                deviceInfo.group_id = null;
                deviceInfo.save();
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

    public Result delete() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String ladder_id = form.get("ladder_id");
            Ladder ladder= Ladder.finder.where()
                    .eq("id", ladder_id)
                    .findUnique();
            if (ladder == null ) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            DeviceInfo deviceInfo=DeviceInfo.finder.where()
                    .eq("ladder_id", ladder_id)
                    .findUnique();
            if (deviceInfo!=null){
                deviceInfo.ladder_id=null;
                Ebean.save(deviceInfo);
            }
            Ebean.delete(ladder);
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_DELETE_FAILED);
        }
    }

    public Result readLadderEvent(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String ladder_id = form.get("id");
            String numStr = form.get("num");
            String pageStr = form.get("page");
            if(ladder_id == null){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Ladder ladder = Ladder.finder.where().idEq(ladder_id).findUnique();
            if(ladder!=null){
                Devices device_one;
                Devices device_two;
                String sqlList = "SELECT * FROM ladder.events WHERE device_id=";

                String sqlCount = "SELECT count(0) as id FROM ladder.events WHERE device_id=";
                String sql ="";
                String startTime = form.get("start");
                String endTime = form.get("end");
                if (ladder.ctrl != null && !ladder.ctrl.isEmpty()){
                    device_one = Devices.finder.where().eq("imei",ladder.ctrl).findUnique();
                    assert device_one != null;
                    sql += device_one.id;
                    if(startTime!=null&&!startTime.isEmpty()){
                        sql += " and time>='"+startTime+"'";
                    }
                    if(endTime!=null&&!endTime.isEmpty()){
                        sql += " and time<='"+endTime+"'";
                    }
                    sql +=" or device_id=";
                }

                if(ladder.door1!=null && !ladder.door1.isEmpty()){
                    device_two = Devices.finder.where().eq("imei",ladder.door1).findUnique();
                    assert device_two != null;
                    sql += +device_two.id;
                }

                if(startTime!=null&&!startTime.isEmpty()){
                    sql += " and time>='"+startTime+"'";
                }
                if(endTime!=null&&!endTime.isEmpty()){
                    sql += " and time<='"+endTime+"'";
                }
                int totalNum=0;
                int totalPage=0;
                List<SqlRow> eventsList;
                if(!numStr.isEmpty()&&!pageStr.isEmpty()){
                    int page = Integer.parseInt(pageStr);
                    int num = Integer.parseInt(numStr);
                    sqlCount += sql;
                    sql += " order by id desc limit "+(page-1)*num+","+num;
                    sqlList += sql;
                    eventsList=Ebean.createSqlQuery(sqlList)
                            .findList();
                    totalNum = Ebean.createSqlQuery(sqlCount).findUnique().getInteger("id");
                    totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
                }else{
                    sqlCount += sql;
                    sql += " order by id desc";
                    sqlList += sql;
                    eventsList=Ebean.createSqlQuery(sqlList)
                            .findList();
                    totalNum = Ebean.createSqlQuery(sqlCount).findUnique().getInteger("id");
                    totalPage = 1;
                }
                return successList(totalNum,totalPage,eventsList);
            }
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
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

    public Result readSimple(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<Ladder> exprList= Ladder.finder.where();
            List<Ladder> ladderInfoList;

            String search_info = form.get("search_info");
            String install_addr = form.get("install_addr");
            String group_id = form.get("group_id");
            String pageStr = form.get("page");
            String numStr = form.get("num");

            if (search_info != null && !search_info.isEmpty()){
                exprList=Ladder.finder.where().contains("ctrl",search_info);
                if(exprList.findRowCount()<1) {
                    exprList = Ladder.finder.where().contains("door1", search_info);
                    if (exprList.findRowCount() < 1) {
                        exprList = Ladder.finder.where().contains("door2", search_info);
                        if (exprList.findRowCount() < 1) {
                            exprList = Ladder.finder.where().contains("name", search_info);
                        }
                    }
                }
            }
            List<FollowLadder> followList= FollowLadder.finder.where().eq("user_id", session("userId")).findList();
            Set<String> ctrllist=new HashSet<>();
            for(FollowLadder follows:followList){
                ctrllist.add(follows.ctrl);
            }
            exprList=exprList.in("ctrl",ctrllist);
            if (group_id != null && !group_id.isEmpty()) {
                exprList.or(Expr.eq("group_id",group_id),Expr.eq("group_id",null));
            }
            if(install_addr!=null&&!install_addr.isEmpty()){
                exprList=exprList.contains("install_addr",install_addr);
            }
            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);

            ladderInfoList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("group_id desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, ladderInfoList);
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
    public Result readSimpleF(){
        try {
            Result ret = readSimple();
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
                Logger.info(String.valueOf(node.get("group_id")));
                if (node.get("group_id").toString()!="null") {
                    node.put("follow","yes");
                } else {
                    node.put("follow","no");
                }
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
    public Result readSimpleS(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<Ladder> exprList= Ladder.finder.where();
            List<Ladder> ladderInfoList;

            String search_info = form.get("search_info");
            String install_addr = form.get("install_addr");
            String group_id = form.get("group_id");
            String pageStr = form.get("page");
            String numStr = form.get("num");

            if (search_info != null && !search_info.isEmpty()){
                exprList=Ladder.finder.where().contains("ctrl",search_info);
                if(exprList.findRowCount()<1) {
                    exprList = Ladder.finder.where().contains("door1", search_info);
                    if (exprList.findRowCount() < 1) {
                        exprList = Ladder.finder.where().contains("door2", search_info);
                        if (exprList.findRowCount() < 1) {
                            exprList = Ladder.finder.where().contains("name", search_info);
                        }
                    }
                }
            }
            List<FollowLadder> followList= FollowLadder.finder.where().eq("user_id", session("userId")).findList();
            Set<String> ctrllist=new HashSet<>();
            for(FollowLadder follows:followList){
                ctrllist.add(follows.ctrl);
            }
            exprList=exprList.in("ctrl",ctrllist);
            if (group_id != null && !group_id.isEmpty()) {
                exprList.add(Expr.eq("group_id",group_id));
            }
            if(install_addr!=null&&!install_addr.isEmpty()){
                exprList=exprList.contains("install_addr",install_addr);
            }
            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);

            ladderInfoList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, ladderInfoList);
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
