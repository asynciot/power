package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Group;


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
public class GroupController extends BaseController{
    @Inject
    private FormFactory formFactory;
    public Result create(){
        try {
            Form<Group> form = formFactory.form(Group.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
            }

            Group groups = form.get();
//            if (groups.name == null || groups.name.isEmpty()) {
//                throw new CodeException(ErrDefinition.E_COMMON_FTP_INCORRECT_PARAM);
//            }
            groups.t_create = new Date();
            Ebean.save(groups);

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
    public Result update(){
        return delete(Group.class,formFactory);
    }
    public Result read(){
        return delete(Group.class,formFactory);
    }
    public Result delete(){
        return delete(Group.class,formFactory);
    }
}
