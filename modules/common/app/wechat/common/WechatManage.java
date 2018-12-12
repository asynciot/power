package wechat.common;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import models.common.MessRecord;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * Created by lengxia on 2018/10/30.
 * 微信处理模块
 */
@Singleton
public class WechatManage {
    @Inject
    private WSClient ws;
    private static int TIME_OUT = 10000;

    public static String Access_token;
    private static final String APPID = "wx72ba32311ca1f28e";
    private static final String APPSECRET = "43500dcde66fd025cdd7a8cdd7fd6e37";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    private static final String ADD_BUTTON="https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;


    public int getbutton=0;
    public void schedules(){

        this.actorSystem.scheduler().schedule(
                Duration.create(5, SECONDS),
                Duration.create(2, HOURS),
                ()-> GetAccessToken(),
                this.executionContext
        );

    }
    public void addbutton(){
        String url=ADD_BUTTON.replace("ACCESS_TOKEN",Access_token).replace("\"","");
        try {
            Map<String, Object> result = new HashMap<String,Object>();
            List<Object> button = new ArrayList<>();
            Map<String, Object> item = new HashMap<String,Object>();
            item.put("type","view");
            item.put("name","电梯系统");
            item.put("key","ladder");
            item.put("url","http://server.asynciot.com");
            button.add(item);
            result.put("button",button);
            CompletionStage<JsonNode> jsonPromise =ws.url(url)
                    .setContentType("application/json")
                    .setRequestTimeout(TIME_OUT)
                    .post(Json.toJson(result))
                    .thenApply(WSResponse::asJson);
            JsonNode retVal = jsonPromise.toCompletableFuture().get();
            String errcode=retVal.get("errcode").toString();
            if(errcode!=null&&Integer.parseInt(errcode)==0){
                getbutton=1;
                Logger.info("create button ok");
            }
            else{
                Logger.info("Get errcode"+errcode);
            }


        }
        catch (Throwable e) {
            Logger.error("post button error");
            e.printStackTrace();
        }
    }

    public void GetAccessToken() {
        String url = ACCESS_TOKEN_URL.replace("APPID", APPID).replace("APPSECRET", APPSECRET);
        try{
            CompletionStage<JsonNode> jsonPromise =ws.url(url)
                    .setContentType("application/json")
                    .setRequestTimeout(TIME_OUT)
                    .get()
                    .thenApply(WSResponse::asJson);
            JsonNode retVal = jsonPromise.toCompletableFuture().get();
            if(retVal.get("access_token")!=null){
                WechatManage.Access_token=retVal.get("access_token").toString();
                Logger.info("Get new access_token: "+ WechatManage.Access_token);
                if(getbutton==0){
                    addbutton();
                }
            }else {
                Logger.info("Get no access_token");
            }

            //SendTmp_on("oI2qB0vxDuJlnM3UrvDB5Gc6P6Ho");
            //SendTmp_on("oI2qB0hfJd93SNlutiNxxOLSKE5E");

        }
        catch (Throwable e) {
            Logger.error("Get access_token error");
            e.printStackTrace();
        }

    }


    @Inject
    public WechatManage(ActorSystem actorSystem,ExecutionContext executionContext){
        this.actorSystem=actorSystem;
        this.executionContext=executionContext;
        this.schedules();
    }



}
