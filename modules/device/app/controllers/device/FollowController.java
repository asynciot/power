package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Devices;
import models.device.Follow;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2018/5/14.
 */

public class FollowController extends BaseController {
    @Inject
    private FormFactory formFactory;

    public Result create() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();

            String userId = session("userId");
            String imei = form.get("imei");
            Follow followInfo = new Follow();

            if(imei==null||imei.isEmpty()){
                throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
            }
            Devices devices=Devices.finder.where().eq("IMEI",imei).findUnique();
            followInfo.imei=imei;
            followInfo.device_id=devices.id;
            followInfo.createTime=new Date();
            followInfo.userId=session("userId");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            int count = Follow.finder.where()
            		.eq("userId", session("userId"))
            		.eq("imei", followInfo.imei)
            		.findRowCount();

            if (count != 0) {
            	throw new CodeException(ErrDefinition.E_FOLLOW_INFO_ALREADY_EXIST);
            }

            /*count=Follow.finder.where().eq("userId", session("userId")).findRowCount();
            Account account=Account.finder.byId(followInfo.userId);
            if(account!=null){
                if(account.maxfollow<=count){
                    throw new CodeException(ErrDefinition.E_ACCOUNT_FOLLOW_OUT_BOUND);
                }
            }*/

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

            List<Follow> followInfoList;
            String id = form.get("id");
            
            if (id != null && !id.isEmpty()) {
                Follow followInfo =Follow.finder.byId(Integer.parseInt(id));
                if(followInfo == null){
                	throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
                }
                followInfoList = new ArrayList<Follow>();
                followInfoList.add(followInfo);
                return successList(1, 1, followInfoList);
            }

            ExpressionList<Follow> exprList = Follow.finder.where();
            String pageStr = form.get("page");
            String numStr = form.get("num");

            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
            }

            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);
            exprList.add(Expr.eq("userId", session("userId")));

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
    public Result delete() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String device_id = form.get("device_id");
            String imei = form.get("imei");
            String ftype = form.get("ftype");
            Follow follow = new Follow();
            if (device_id!=null&&!device_id.isEmpty()){
               follow = Follow.finder.where()
                        .eq("userId", session("userId"))
                        .eq("device_id", device_id)
                        .eq("ftype", ftype)
                        .findUnique();
            }
            if (imei!=null&&!imei.isEmpty()){
                follow = Follow.finder.where()
                        .eq("userId", session("userId"))
                        .eq("imei", imei)
                        .eq("ftype", ftype)
                        .findUnique();
            }

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
