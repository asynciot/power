package controllers.account;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.CodeException;
import controllers.common.CodeGenerator;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import models.account.Functions;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by lengxia on 2018/10/30.
 */
public class FunctionsController extends XDomainController {
    @Inject
    public FormFactory formFactory;

    public Result create() {
        try {
            Form<Functions> form = formFactory.form(Functions.class).bindFromRequest();
            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Functions functions = form.get();
            if(functions.name==null||functions.name.isEmpty()){
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Functions pd = Functions.finder.where().eq("name",functions.name).findUnique();
            if(pd != null){
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Ebean.save(functions);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }

    public Result read(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<Functions> exprList= Functions.finder.where();
            List<Functions> functionsList = new ArrayList<Functions>();

            String pageStr = form.get("page");
            String numStr = form.get("num");
            String id = form.get("id");

            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (id != null && !id.isEmpty()) {
                exprList=exprList.contains("id",id);
            }
            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);

            functionsList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, functionsList);
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

    public Result update() {
        try {
            Form<Functions> forms = formFactory.form(Functions.class).bindFromRequest();

            Functions functions = forms.get();
            if (null == functions.id || functions.id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Ebean.update(functions);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }
}
