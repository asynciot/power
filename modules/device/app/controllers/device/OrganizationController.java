package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Organization;
import models.device.Organize;
import models.account.Account;

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
import java.util.Random;

/**
 * Created by lengxia on 2019/3/2.
 */
public class OrganizationController extends BaseController{
    @Inject
    private FormFactory formFactory;
    public Result create(){
        try {
            Form<Organization> form = formFactory.form(Organization.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
            }

            Organization organization = form.get();

            Random rand = new Random();
            organization.number = (rand.nextInt(90000000)+10000000);
//            if (organize.name == null || organize.name.isEmpty()) {
//                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
//            }
            organization.t_create = new Date();
            Ebean.save(organization);

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
            ExpressionList<Organization> exprList= Organization.finder.where();
            List<Organization> organize = new ArrayList<Organization>();

            String username = form.get("username");
            String pageStr = form.get("page");
            String numStr = form.get("nums");
            String id = form.get("id");
            String numbers = form.get("number");

            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if(username != null){
                exprList=exprList.eq("leader",username);
            }
            if (numbers != null) {
                Integer number = Integer.parseInt(form.get("number"));
                exprList=exprList.eq("number",number);
            }
            if (id != null && !id.isEmpty()) {
                exprList=exprList.contains("id",id);
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
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }
    public Result update(){
        try {
            Form<Organization> forms = formFactory.form(Organization.class).bindFromRequest();

            Organization organization = forms.get();
//            if (null == organize.id || organize.id.isEmpty()) {
//                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
//            }
            Ebean.update(organization);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }
    public Result joinGroup(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String userId = session("userId");

            Account account = Account.finder.where().eq("id",userId).findUnique();
            Integer number = Integer.parseInt(form.get("number"));

            if (null == number) {
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Organization organization = Organization.finder.where().eq("number",number).findUnique();
            account.organization_id = organization.id.toString();

            Ebean.update(account);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }
    public Result rmGroup(){
        try {
            String userId = session("userId");
            if (null == userId || userId.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Account account = Account.finder.where().eq("id",userId).findUnique();
            account.organization_id = "0";

            Ebean.update(account);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }
    public Result bindGroup(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String id = form.get("id");
            String group_id = form.get("group_id");
            if (null == id || id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            if (null == group_id || group_id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_FUNCTION_INFO_INCORRECT_PARAM);
            }
            Organization organization = Organization.finder.where().eq("id",id).findUnique();
            organization.organize_id = group_id;
            Ebean.update(organization);
            Organize organize = Organize.finder.where().eq("id",group_id).findUnique();
            organize.group_id = id;
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
            Organization organization = Organization.finder.where().eq("id",id).findUnique();

            Ebean.delete(organization);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_FUNCTION_INFO_CREATE_FAILED);
        }
    }
}
