package controllers.device;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;

import models.account.Account;
import models.device.Cellocation;
import models.device.DeviceInfo;
import models.device.Ladder;
import models.device.Devices;
import models.device.IPlocation;
import models.device.FollowLadder;

import play.Logger;
import play.core.j.JavaResultExtractor;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by lengxia on 2018/10/30.
 */
public class LadderController extends BaseController {
    @Inject
    private FormFactory formFactory;

    @Inject
    WSClient ws;
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
            Devices devices = new Devices();
            if (!ladderInfo.ctrl.isEmpty()) {
                devices = Devices.finder.where().eq("IMEI", ladderInfo.ctrl).findUnique();
                ladderInfo.ctrl_id = devices.id.toString();
            }else{
                devices = Devices.finder.where().eq("IMEI", ladderInfo.door1).findUnique();
                ladderInfo.ctrl_id = devices.id.toString();
            }
            DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("IMEI", devices.IMEI).findUnique();
            ladderInfo.state = deviceInfo.state;
            Ebean.save(ladderInfo);
            deviceInfo.ladder_id = ladderInfo.id.toString();
            Ebean.save(deviceInfo);

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
            List<Ladder> ladderInfoList = new ArrayList<Ladder>();

            String search_info = form.get("search_info");
            String ladder_id = form.get("id");
            String register=form.get("register");
            String tabcor = form.get("tagcolor");
            String state = form.get("state");
            String pageStr = form.get("page");
            String numStr = form.get("num");
            String follow=form.get("follow");
            String install_addr=form.get("install_addr");

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
                List<FollowLadder> followList= FollowLadder.finder.where().eq("userId", session("userId")).findList();
                Set<String> ctrllist=new HashSet<>();
                Set<String> door1list=new HashSet<>();
                Set<String> door2list=new HashSet<>();
                for(FollowLadder follows:followList){
                    ctrllist.add(follows.ctrl);
                    door1list.add(follows.door1);
                    door2list.add(follows.door2);
                }
                exprList=exprList.in("ctrl",ctrllist);
                exprList=exprList.in("door1",door1list);
                exprList=exprList.in("door2",door2list);
            }
            if (state != null && !state.isEmpty()) {
                exprList=exprList.contains("state",state);
            }
            if (tabcor != null && !tabcor.isEmpty()) {
                exprList=exprList.contains("tagcolor",tabcor);
            }
            if(register!=null&&!register.isEmpty()){
                exprList=exprList.eq("register",register);
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

            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);

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
            List<ObjectNode> nodeList = new ArrayList<ObjectNode>();
            for (JsonNode child : resultData.get("data").get("list")) {
                ObjectNode node = (ObjectNode) new ObjectMapper().readTree(child.toString());
                if(node.get("ctrl").asText() != ""){
                    Ladder ladder = Ladder.finder.where().eq("ctrl",node.get("ctrl").asText()).findUnique();
                    if(ladder !=null ) {
                        node.put("name", ladder.name);
                        DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei", node.get("ctrl").asText()).findUnique();
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
                    }
                }else {
                    Ladder ladder = Ladder.finder.where().eq("door1", node.get("door1").asText()).findUnique();
                    if (ladder != null) {
                        node.put("name", ladder.name);
                        DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei", node.get("door1").asText()).findUnique();
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
            if(name!=null&&!name.isEmpty()){
                ladder.name=name;
            }
            if(install_addr!=null&&!install_addr.isEmpty()){
                ladder.install_addr=install_addr;
            }
            DeviceInfo deviceInfo = new DeviceInfo();
            if(type!=null&&!type.isEmpty()){
                if(type.equals("1")){
                    deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.ctrl).findUnique();
                    deviceInfo.ladder_id = null;
                    Ebean.save(deviceInfo);
                    ladder.ctrl=IMEI;
                    deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.ctrl).findUnique();
                    deviceInfo.ladder_id = ladder_id;
                    ladder.ctrl_id = deviceInfo.id.toString();
                }else if(type.equals("2")){
                    deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.door1).findUnique();
                    deviceInfo.ladder_id = null;
                    Ebean.save(deviceInfo);
                    ladder.door1=IMEI;
                    deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.door1).findUnique();
                    deviceInfo.ladder_id = ladder_id;
                    ladder.ctrl_id = deviceInfo.id.toString();
                }else if(type.equals("3")){
                    deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.door2).findUnique();
                    deviceInfo.ladder_id = null;
                    Ebean.save(deviceInfo);
                    ladder.door2=IMEI;
                    deviceInfo = DeviceInfo.finder.where().eq("IMEI", ladder.door2).findUnique();
                    deviceInfo.ladder_id = ladder_id;
                    ladder.ctrl_id = deviceInfo.id.toString();
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
            deviceInfo.ladder_id=null;
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
            return failure(ErrDefinition.E_COMMON_DELETE_FAILED );
        }
    }

}
