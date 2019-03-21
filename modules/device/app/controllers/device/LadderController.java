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
import models.device.Cellocation;
import models.device.DeviceInfo;
import models.device.Ladder;
import models.device.Devices;
import models.device.IPlocation;
import models.device.Follow;
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

            if (ladderInfo.door1 != null && !ladderInfo.ctrl.isEmpty() && ladderInfo.ctrl == null && ladderInfo.ctrl.isEmpty()){
                Devices devices = Devices.finder.where().eq("IMEI", ladderInfo.door1).findUnique();
                ladderInfo.t_logon = devices.t_logon;
                ladderInfo.t_logout = devices.t_logout;
                ladderInfo.cell_mcc = devices.cell_mcc;
                ladderInfo.cell_mnc = devices.cell_mnc;
                ladderInfo.cell_lac = devices.cell_lac;
                ladderInfo.cell_cid = devices.cell_cid;
                ladderInfo.ipaddr = devices.ipaddr;
            }

            if (ladderInfo.ctrl != null && !ladderInfo.ctrl.isEmpty()) {
                Devices devices = Devices.finder.where().eq("IMEI", ladderInfo.ctrl).findUnique();
                ladderInfo.t_logon = devices.t_logon;
                ladderInfo.t_logout = devices.t_logout;
                ladderInfo.cell_mcc = devices.cell_mcc;
                ladderInfo.cell_mnc = devices.cell_mnc;
                ladderInfo.cell_lac = devices.cell_lac;
                ladderInfo.cell_cid = devices.cell_cid;
                ladderInfo.ipaddr = devices.ipaddr;
            }

            ladderInfo.t_create = new Date();

            Ebean.save(ladderInfo);

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

            String IMEI = form.get("IMEI");
            String search_info = form.get("search_info");
            String register=form.get("register");
            String tabcor = form.get("tagcolor");
            String state = form.get("state");
            String pageStr = form.get("page");
            String numStr = form.get("num");
            String follow=form.get("follow");
            String install_addr=form.get("install_addr");

            if (IMEI != null && !IMEI.isEmpty()) {
                Ladder ladder = Ladder.finder.where().eq("ctrl",IMEI).eq("door1", IMEI).eq("door2", IMEI).findUnique();
                if(ladder!=null){
                    ladderInfoList.add(ladder);
                }
                return successList(ladderInfoList.size(), 1, ladderInfoList);
            }

            if (search_info != null && !search_info.isEmpty()){
                exprList=Ladder.finder.where().contains("ctrl",search_info);
                exprList=Ladder.finder.where().contains("door1",search_info);
                exprList=Ladder.finder.where().contains("door2",search_info);
                if(exprList.findRowCount()<1){
                    exprList=Ladder.finder.where().contains("name",search_info);
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

            if (state != null && !state.isEmpty()) {
                exprList=exprList.eq("state",state);
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

    public Result update() {
        return update(Ladder.class, formFactory);
    }
    public Result delete() {
        return delete(Ladder.class, formFactory);
    }

}
