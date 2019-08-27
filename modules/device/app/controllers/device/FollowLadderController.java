package controllers.device;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;

import models.account.Account;
import models.device.Devices;
import models.device.Ladder;
import models.device.Cellocation;
import models.device.DeviceInfo;
import models.device.IPlocation;
import models.device.FollowLadder;

import play.Configuration;
import play.Logger;
import play.core.j.JavaResultExtractor;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.ws.WSClient;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2018/5/14.
 */

public class FollowLadderController extends BaseController {
    @Inject
    private FormFactory formFactory;

    @Inject
    private Materializer mat;

    private static int TIME_OUT = 10000;

    public Result create() {
        try {
            Form<FollowLadder> form = formFactory.form(FollowLadder.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
            }

            FollowLadder followInfo = form.get();
            if(followInfo.ctrl!=null){
                Ladder ladder=Ladder.finder.where().eq("ctrl",followInfo.ctrl).findUnique();
                if (ladder!=null)
                    followInfo.ladder_id=ladder.id;
            }else{
                Ladder ladder=Ladder.finder.where().eq("door1",followInfo.door1).findUnique();
                if (ladder!=null)
                    followInfo.ladder_id=ladder.id;
            }
            followInfo.createTime=new Date();
            followInfo.user_id=session("userId");

            Ebean.save(followInfo);

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
    public Result read() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();

            ExpressionList<FollowLadder> exprList = FollowLadder.finder.where();
            List<FollowLadder> followInfoList;
            String id = form.get("id");
            String pageStr = form.get("page");
            String numStr = form.get("num");

            if (id != null && !id.isEmpty()) {
                FollowLadder followInfo =FollowLadder.finder.byId(Integer.parseInt(id));
                if(followInfo == null){
                    throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
                }
                followInfoList = new ArrayList<>();
                followInfoList.add(followInfo);
                return successList(1, 1, followInfoList);
            }
            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
            }

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);
            exprList.add(Expr.eq("user_id", session("userId")));

            followInfoList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, followInfoList);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FOLLOW_INFO_READ_FAILED);
        }
    }

    public Result readMore() {
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
                Ladder ladder = Ladder.finder.where().eq("ctrl",node.get("ctrl").asText()).findUnique();
                if(ladder !=null){
                    node.put("ladder_id", ladder.id);
                    DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei",node.get("ctrl").asText()).findUnique();
                    if(deviceInfo!=null){
                        node.put("rssi",deviceInfo.rssi);
                        node.put("cellocation_id",deviceInfo.cellocation_id);
                        node.put("IPlocation_id",deviceInfo.IPlocation_id);
                    }
                    Cellocation cellocation=Cellocation.finder.where().eq("id",deviceInfo.cellocation_id).findUnique();
                    IPlocation iplocation=IPlocation.finder.where().eq("id",deviceInfo.IPlocation_id).findUnique();
                    if(cellocation!=null){
                        node.put("cell_lat",cellocation.lat);
                        node.put("cell_lon",cellocation.lon);
                        node.put("cell_radius",cellocation.radius);
                        node.put("cell_address",cellocation.address);
                    }
                    if(iplocation!=null){
                        node.put("ip_ip", iplocation.ip);
                        node.put("ip_city",iplocation.city);
                        node.put("ip_country", iplocation.country);
                        node.put("ip_region",iplocation.region);
                    }
                }else{
                    ladder = Ladder.finder.where().eq("door1",node.get("door1").asText()).findUnique();
                    node.put("ladder_id", ladder.id);
                    DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("imei",node.get("door1").asText()).findUnique();
                    if(deviceInfo!=null){
                        node.put("rssi",deviceInfo.rssi);
                        node.put("cellocation_id",deviceInfo.cellocation_id);
                        node.put("IPlocation_id",deviceInfo.IPlocation_id);
                    }
                    Cellocation cellocation=Cellocation.finder.where().eq("id",deviceInfo.cellocation_id).findUnique();
                    IPlocation iplocation=IPlocation.finder.where().eq("id",deviceInfo.IPlocation_id).findUnique();
                    if(cellocation!=null){
                        node.put("cell_lat",cellocation.lat);
                        node.put("cell_lon",cellocation.lon);
                        node.put("cell_radius",cellocation.radius);
                        node.put("cell_address",cellocation.address);
                    }
                    if(iplocation!=null){
                        node.put("ip_ip", iplocation.ip);
                        node.put("ip_city",iplocation.city);
                        node.put("ip_country", iplocation.country);
                        node.put("ip_region",iplocation.region);
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

    public Result delete() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String ladder_id = form.get("ladder_id");
            FollowLadder follow= FollowLadder.finder.where()
                    .eq("userId", session("userId"))
                    .eq("ladder_id", ladder_id)
                    .findUnique();
            if (follow == null ) {
                throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
            }
            Ebean.delete(follow);
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FOLLOW_INFO_DELETE_FAILED);
        }
    }
}
