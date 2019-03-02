package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Group;
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
 * Created by lengxia on 2019/3/2.
 */
public class GroupController extends BaseController{
    @Inject
    private FormFactory formFactory;
    public Result delete(){
        return delete(Group.class,formFactory);
    }
    public Result create(){
        return delete(Group.class,formFactory);
    }
    public Result update(){
        return delete(Group.class,formFactory);
    }
    public Result read(){
        return delete(Group.class,formFactory);
    }
}
