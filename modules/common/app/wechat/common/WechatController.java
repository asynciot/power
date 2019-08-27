package wechat.common;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.XDomainController;
import models.common.MessRecord;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Created by lengxia on 2018/11/7.
 * 微信管理模块
 */
public class WechatController extends XDomainController {
    @Inject
    WSClient ws;
    private static int TIME_OUT = 10000;
    private static final String POST_TMP_URL="https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";
    public WechatController( WSClient ws) {
        this.ws = ws;
    }
    public WechatController() {
    }
    public Result getqrcode() {

        String url="https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+WechatManage.Access_token.replace("\"","");
        Map<String, Object> result = new HashMap<>();
        result.put("expire_seconds","604800");
        result.put("action_name","QR_STR_SCENE");
        Map<String, Object> action_info = new HashMap<>();
        Map<String, Object> scene = new HashMap<>();
        scene.put("scene_str",session().get("userId"));
        action_info.put("scene",scene);
        result.put("action_info",action_info);

        try{
            CompletionStage<JsonNode> jsonPromise =ws.url(url)
                    .setContentType("application/json")
                    .setRequestTimeout(TIME_OUT)
                    .post(Json.toJson(result))
                    .thenApply(WSResponse::asJson);
            JsonNode retVal = jsonPromise.toCompletableFuture().get();
            String ticket=retVal.get("ticket").toString();
            Logger.info("Get ticket: "+ticket);

            result.clear();
            result.put("url","https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="+ticket.replace("\"",""));
            return success("qrcode",result);

        }
        catch (Throwable e) {
            Logger.error("Get qrcode Error");
            e.printStackTrace();
        }
        return success();
    }


    public void SendTmp_on(String touser,Integer code,Integer type,Integer device_id,String producer,String userId,Integer order_id,String device_name) {
        Map<String, Object> result = new HashMap<>();

        result.put("touser",touser);
        result.put("template_id","7Do5VrpCMm6XGAbzBM6wHRPXxiqThdslYTvMadIkKhE");
        result.put("url","http://server.asynciot.com/company/order/"+order_id);

        Map<String, Object> data = new HashMap<>();

        Map<String, Object> first = new HashMap<>();
        first.put("value","电梯事件推送消息");
        first.put("color","#173177");
        data.put("first",first);

        Map<String, Object> OrderSn = new HashMap<>();
        OrderSn.put("value",device_name+" (电梯名称)");
        OrderSn.put("color","#173177");
        data.put("OrderSn",OrderSn);

        String type_str=null;
        if(type==1)type_str="故障提示";
        if(type==2)type_str="维保提示";
        if(type==3)type_str="检验提示";

        Map<String, Object> OrderStatus = new HashMap<>();
        OrderStatus.put("value",type_str+" (事件类型)，"+Integer.toHexString(code)+" (事件代码)");
        OrderStatus.put("color","#173177");
        data.put("OrderStatus",OrderStatus);

        Map<String, Object> remark = new HashMap<>();
        remark.put("value","下单人:"+producer);
        remark.put("color","#173177");
        data.put("remark",remark);

        result.put("data",data);
        String url=POST_TMP_URL.replace("ACCESS_TOKEN",WechatManage.Access_token).replace("\"","");

        try {
            CompletionStage<JsonNode> jsonPromise =ws.url(url)
                    .setContentType("application/json")
                    .setRequestTimeout(TIME_OUT)
                    .post(Json.toJson(result))
                    .thenApply(WSResponse::asJson);
            JsonNode retVal = jsonPromise.toCompletableFuture().get();
            String errcode=retVal.get("errcode").toString();
            Logger.info("Get errcode: "+errcode);

            MessRecord messRecord=new MessRecord();
            messRecord.createTime=new Date();
            messRecord.device_id=device_id;
            messRecord.title="电梯推送消息";
            messRecord.toId=userId;
            messRecord.type=1;
            messRecord.save();
        }
        catch (Throwable e) {
            Logger.error("post temp error");
            e.printStackTrace();
        }
    }
}
