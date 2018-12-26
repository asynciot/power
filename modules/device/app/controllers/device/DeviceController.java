package controllers.device;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Cellocation;
import models.device.DeviceInfo;
import models.device.Devices;
import models.device.IPlocation;
import models.device.Follow;
import play.Logger;
import play.core.j.JavaResultExtractor;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lengxia on 2018/10/30.
 */
public class DeviceController extends BaseController {
    @Inject
    private FormFactory formFactory;

    @Inject
    WSClient ws;
    @Inject
    private Materializer mat;

    private static int TIME_OUT = 10000;
    public Result create() {
        return create(Devices.class, formFactory);
    }
    public Result read(){
        try {

            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<DeviceInfo> exprList= DeviceInfo.finder.where();
            List<DeviceInfo> deviceInfoList = new ArrayList<DeviceInfo>();

            String IMEI = form.get("IMEI");
            String search_info = form.get("search_info");
            String register=form.get("register");
            String tabcor = form.get("tagcolor");
            String state = form.get("state");
            String device_type = form.get("device_type");
            String pageStr = form.get("page");
            String numStr = form.get("num");
            String follow=form.get("follow");
            String device_id=form.get("device_id");
            if (device_id != null && !device_id.isEmpty()) {
                DeviceInfo deviceInfo = DeviceInfo.finder.byId(Integer.parseInt(device_id));
                if(deviceInfo!=null){
                    deviceInfoList.add(deviceInfo);
                }
                return successList(deviceInfoList.size(), 1, deviceInfoList);
            }

            if (IMEI != null && !IMEI.isEmpty()) {
                DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("IMEI",IMEI).findUnique();
                if(deviceInfo!=null){
                    deviceInfoList.add(deviceInfo);
                }
                return successList(deviceInfoList.size(), 1, deviceInfoList);
            }

            if (search_info != null && !search_info.isEmpty()){
                exprList=DeviceInfo.finder.where().contains("IMEI",search_info);
                if(exprList.findRowCount()<1){
                    exprList=DeviceInfo.finder.where().contains("device_name",search_info);
                }
            }

            if(follow!=null&&!follow.isEmpty()&&follow.equals("yes")){
                List<Follow> followList= Follow.finder.where().eq("userId", session("userId")).findList();
                Set<String> imeilist=new HashSet<>();
                for(Follow follows:followList){
                    imeilist.add(follows.imei);
                }
                exprList=exprList.in("IMEI",imeilist);
            }

            if (tabcor != null && !tabcor.isEmpty()) {
                exprList=exprList.contains("tagcolor",tabcor);
            }
            if (state != null && !state.isEmpty()) {
                exprList=exprList.eq("state",state);
            }
            if (device_type != null && !device_type.isEmpty()) {
                exprList=exprList.eq("device_type",device_type);
            }

            if(register!=null&&!register.isEmpty()){
                exprList=exprList.eq("register",register);
            }

            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);

            deviceInfoList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, deviceInfoList);
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
                Devices devices = Devices.finder.where().eq("IMEI",node.get("IMEI").asText()).findUnique();
                Cellocation cellocation=Cellocation.finder.where().eq("id",node.get("cellocation_id").asInt()).findUnique();
                IPlocation iplocation=IPlocation.finder.where().eq("id",node.get("IPlocation_id").asInt()).findUnique();
                if(cellocation!=null){
                    node.put("cell_lat",cellocation.lat);
                    node.put("cell_lon",cellocation.lon);
                    node.put("cell_radius",cellocation.radius);
                    node.put("cell_address",cellocation.address);
                }
                if(devices !=null){
                    node.put("device_id", devices.id);
                    node.put("device_IMSI", devices.IMSI);
                    node.put("device_model", devices.model);
                    node.put("device_firmware", devices.firmware);
                }
                if(iplocation!=null){
                    node.put("ip_ip", iplocation.ip);
                    node.put("ip_city",iplocation.city);
                    node.put("ip_country", iplocation.country);
                    node.put("ip_region",iplocation.region);
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
            String device_id = form.get("device_id");
            String device_name = form.get("device_name");
            String install_addr = form.get("install_addr");
            String install_date = form.get("install_date");
            String maintenance_cycle = form.get("maintenance_cycle");
            String maintenance_type = form.get("maintenance_type");

            if (device_id == null || device_id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            DeviceInfo deviceInfo=DeviceInfo.finder.byId(Integer.parseInt(device_id));
            if(!device_name.isEmpty()&&device_name!=null){
                deviceInfo.device_name=device_name;
            }
            if(!install_addr.isEmpty()&&install_addr!=null){
                deviceInfo.install_addr=install_addr;
            }
            if(!install_date.isEmpty()&&install_date!=null){
                deviceInfo.install_date=install_date;
            }
            if(!maintenance_cycle.isEmpty()&&maintenance_cycle!=null){
                deviceInfo.maintenance_cycle=maintenance_cycle;
            }
            if(!maintenance_type.isEmpty()&&maintenance_type!=null){
                deviceInfo.maintenance_type=maintenance_type;
            }

            deviceInfo.save();
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
        return delete(Devices.class, formFactory);
    }
    public Result readCountfollow() {

            List<Follow> followList= Follow.finder.where().eq("userId", session("userId")).findList();
            Set<String> imeilist=new HashSet<>();
            for(Follow follows:followList){
                imeilist.add(follows.imei);
            }

        try {
            int ctrlonline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 240)
                    .eq("state", "online")
                    .in("IMEI",imeilist)
                    .findRowCount();
            int dooronline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 15)
                    .eq("state", "online")
                    .in("IMEI",imeilist)
                    .findRowCount();
            int ctrloffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 240)
                    .eq("state", "offline")
                    .in("IMEI",imeilist)
                    .findRowCount();
            int dooroffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 15)
                    .eq("state", "offline")
                    .in("IMEI",imeilist)
                    .findRowCount();

            int ctrllongoffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 240)
                    .eq("state", "longoffline")
                    .in("IMEI",imeilist)
                    .findRowCount();
            int doorlongoffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 15)
                    .eq("state", "longoffline")
                    .in("IMEI",imeilist)
                    .findRowCount();

            ObjectNode node = Json.newObject();
            node.put("ctrlonline", String.format("%d", ctrlonline));
            node.put("dooronline", String.format("%d", dooronline));
            node.put("ctrloffline", String.format("%d", ctrloffline));
            node.put("dooroffline", String.format("%d", dooroffline));
            node.put("ctrllongoffline", String.format("%d", ctrllongoffline));
            node.put("doorlongoffline", String.format("%d", doorlongoffline));
            return success("data", node);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_MESSAGE_READ_FAILED);
        }
    }
    public Result readCount() {
        try {
            int ctrlonline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 240)
                    .eq("state", "online")
                    .findRowCount();
            int dooronline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 15)
                    .eq("state", "online")
                    .findRowCount();
            int ctrloffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 240)
                    .eq("state", "offline")
                    .findRowCount();
            int dooroffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 15)
                    .eq("state", "offline")
                    .findRowCount();

            int ctrllongoffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 240)
                    .eq("state", "longoffline")
                    .findRowCount();
            int doorlongoffline =DeviceInfo.finder
                    .where()
                    .eq("device_type", 15)
                    .eq("state", "longoffline")
                    .findRowCount();

            ObjectNode node = Json.newObject();
            node.put("ctrlonline", String.format("%d", ctrlonline));
            node.put("dooronline", String.format("%d", dooronline));
            node.put("ctrloffline", String.format("%d", ctrloffline));
            node.put("dooroffline", String.format("%d", dooroffline));
            node.put("ctrllongoffline", String.format("%d", ctrllongoffline));
            node.put("doorlongoffline", String.format("%d", doorlongoffline));
            return success("data", node);
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_MESSAGE_READ_FAILED);
        }
    }
}
