package controllers.account;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import models.account.Account;
import models.account.Roles;
import models.account.Functions;
import models.account.Menus;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by lengxia on 2018/10/30.
 */
public class RolesController extends XDomainController {
    @Inject
    public FormFactory formFactory;

    public Result create() {
        try {
            Form<Roles> form = formFactory.form(Roles.class).bindFromRequest();
            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
            }
            Roles roles = form.get();
            if(roles.name==null||roles.name.isEmpty()){
                throw new CodeException(ErrDefinition.E_ROLES_INFO_INCORRECT_PARAM);
            }
            Menus menus = Menus.finder.where().eq("name",roles.name).findUnique();
            Functions functions = Functions.finder.where().eq("name",roles.name).findUnique();
            if(menus!=null && functions!=null){
                roles.menus = menus.id;
                roles.functions = functions.id;
                Ebean.save(roles);
            }
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ROLES_INFO_CREATE_FAILED);
        }
    }

    public Result read(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<Roles> exprList= Roles.finder.where();
            List<Roles> rolesList;

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
            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);

            rolesList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, rolesList);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ROLES_INFO_READ_FAILED);
        }
    }

    public Result confer() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();

            String userId = form.get("userId");
            String id = form.get("id");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            if (id == null || id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_ROLES_INFO_INCORRECT_PARAM);
            }
            Account account = Account.finder.where().eq("id",userId).findUnique();
            if (account!=null){
                account.role = id;
                Ebean.save(account);
            }
            return success();
        } catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }

    public Result delete() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();

            String id = form.get("id");
            if (id == null || id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_ROLES_INFO_INCORRECT_PARAM);
            }
            Roles roles = Roles.finder.where().eq("id", id).findUnique();
            Menus menus = Menus.finder.where().eq("id",id).findUnique();
            Functions functions = Functions.finder.where().eq("id",id).findUnique();
            Ebean.delete(roles);
            Ebean.delete(menus);
            Ebean.delete(functions);
            return success();
        } catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_READ_FAILED);
        }
    }

}
