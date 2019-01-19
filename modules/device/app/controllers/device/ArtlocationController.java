package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Artlocation;
import play.Logger;
import play.api.mvc.Session;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2019/1/19.
 */
public class ArtlocationController extends BaseController{
    @Inject
    private FormFactory formFactory;

    public Result create(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            Artlocation artlocation=new Artlocation();
            String user_id= session("userId");
            if(user_id==null){
                throw  new CodeException((ErrDefinition.E_ACCOUNT_UNAUTHENTICATED));
            }
            String lat=form.get("lat");
            String lon=form.get("lon");
            String t_create=new Date().toString();
            if(lat==null||lat.isEmpty()||lon==null||lon.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            artlocation.lat=lat;
            artlocation.lon=lon;
            artlocation.user_id=user_id;
            artlocation.t_create=t_create;
            Ebean.save(artlocation);
            return success();
        } 	catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_CREATE_FAILED);
        }


    }public Result delete(){
        return delete(Artlocation.class,formFactory);
    }
    public Result read() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Artlocation> artlocationList = null;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Artlocation artlocation = Artlocation.finder.byId(Integer.parseInt(id));
                if (artlocation == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }

                artlocationList = new ArrayList<Artlocation>();
                artlocationList.add(artlocation);
                return successList(1, 1, artlocationList);
            }

            ExpressionList<Artlocation> exprList = Artlocation.finder.where();
            String pageStr = form.get("page");
            String numStr = form.get("num");


            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);

            String user_id = form.get("user_id");
            if (user_id != null && !user_id.isEmpty()) {
                exprList.add(Expr.eq("user_id", user_id));
            }

            artlocationList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("t_create desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, artlocationList);
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
