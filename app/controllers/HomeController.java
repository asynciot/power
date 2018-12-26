package controllers;

import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import models.account.Account;
import models.device.DeviceInfo;
import models.device.Fault;
import models.device.Follow;
import org.w3c.dom.Document;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.XPath;
import play.libs.ws.WSClient;
import play.mvc.*;

import sun.rmi.runtime.Log;
import views.html.*;
import wechat.common.WechatController;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends XDomainController {
    @Inject
    private FormFactory formFactory;
    @Inject
    WSClient ws;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>device.other.routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        DynamicForm form = formFactory.form().bindFromRequest();
        String echostr=form.get("echostr");
        if(echostr!=null){
            return ok(echostr);
        }
        return resource("");
    }
    public Result resource(String filePath) {
        String physicalPath = "public/dist/";
        InputStream is = Play.application().resourceAsStream(physicalPath + "index.html");
        response().setHeader("Content-Type","text/html; charset=UTF-8");
        return ok(is);
    }
    public Result getwechat() {

        Document dom =request().body().asXml();

        if(dom!=null){

            String ToUserName= XPath.selectText("//ToUserName",dom);
            String FromUserName= XPath.selectText("//FromUserName",dom);
            String CreateTime= XPath.selectText("//CreateTime",dom);
            String MsgType= XPath.selectText("//MsgType",dom);
            if(MsgType.equals("event")){
                String Event= XPath.selectText("//Event",dom);
                if(Event.equals("subscribe")){
                    String EventKey= XPath.selectText("//EventKey",dom);
                    EventKey=EventKey.substring(8);
                    Account account= Account.finder.where().eq("id",EventKey).findUnique();
                    if(account!=null){
                        account.wechat_id=FromUserName;
                        account.update();
                    }
                }
            }
            /*
            String EventKey= XPath.selectText("//EventKey",dom);
            Logger.info("get info from wechat "+FromUserName+"\n Get Key : "+EventKey);

            String recs=String.format("<xml>" +
                    "<ToUserName><![CDATA[%s]]></ToUserName>" +
                    "<FromUserName><![CDATA[%s]]></FromUserName>" +
                    "<CreateTime>%s</CreateTime>" +
                    "<MsgType><![CDATA[text]]></MsgType>" +
                    "<Content><![CDATA[%s]]></Content>"+
                    "</xml>",FromUserName,ToUserName,CreateTime,"hello");

            */
        }
        return ok("success");


    }
    public Result getalert(){
        DynamicForm form = formFactory.form().bindFromRequest();
        try{
            String alert = form.get("alert");
            String device_id = form.get("device_id");
            String device_type = form.get("device_type");

            if(alert==null&&alert.isEmpty()&&device_id==null&&device_id.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            Logger.info("get alert "+alert+" from id"+device_id);
            if(alert.equals("0")){
                Logger.info("no alert");
                return ok("no alert");
            }
            Fault fault=new Fault();
            fault.device_id=Integer.parseInt(device_id);
            fault.type=Integer.parseInt(alert);
            fault.device_type=device_type;
            fault.createTime=new Date();
            fault.state="untreated";
            int count = Fault.finder.where()
                    .eq("device_id", fault.device_id)
                    .eq("type", fault.type)
                    .eq("state","untreated")
                    .findRowCount();
            if (count != 0) {
                return  ok("already in db");
            }
            fault.save();

            WechatController wechatController=new WechatController(ws);
            List<Follow> followList=Follow.finder.where().eq("device_id",device_id).findList();
            for(Follow follow:followList){
                Account account=Account.finder.byId(follow.userId);
                if(account.wechat_id==null)continue;
                wechatController.SendTmp_on(account.wechat_id,alert,"",Integer.parseInt(device_id),account.id);
            }
            return ok("ok");
        }catch (CodeException ce) {
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
