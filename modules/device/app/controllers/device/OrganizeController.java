package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Organize;


import play.Logger;
import play.data.Form;
import play.api.mvc.Session;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Created by lengxia on 2019/3/2.
 */
public class OrganizeController extends BaseController{
    @Inject
    private FormFactory formFactory;
    public Result create(){
        try {
            Form<Organize> form = formFactory.form(Organize.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
            }

            Organize organize = form.get();
//            if (organize.name == null || organize.name.isEmpty()) {
//                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
//            }
            organize.t_create = new Date();
            Ebean.save(organize);

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
            ExpressionList<Organize> exprList= Organize.finder.where();
            List<Organize> organize = new ArrayList<Organize>();

            String pageStr = form.get("page");
            String numStr = form.get("nums");
            String name = form.get("name");

            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (name != null && !name.isEmpty()) {
                exprList=exprList.contains("name",name);
            }

            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);

            organize = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, organize);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_READ_FAILED);
        }
    }
    public Result update(){
        try {
            Form<Organize> forms = formFactory.form(Organize.class).bindFromRequest();

            Organize organize = forms.get();
//            if (null == organize.id || organize.id.isEmpty()) {
//                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
//            }
            Ebean.update(organize);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }
    public Result delete()
    {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();

            String id = form.get("id");
            if (null == id || id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Organize organize = Organize.finder.where().eq("id",id).findUnique();

            Ebean.delete(organize);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }
}
