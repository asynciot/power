package controllers.account;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import models.account.Menus;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;


/**
 * Created by lengxia on 2018/10/30.
 */
public class MenusController extends XDomainController {
    @Inject
    public FormFactory formFactory;

    public Result create() {
        try {
            Form<Menus> form = formFactory.form(Menus.class).bindFromRequest();
            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_MENU_INFO_INCORRECT_PARAM);
            }
            Menus menus = form.get();
            if(menus.name==null||menus.name.isEmpty()){
                throw new CodeException(ErrDefinition.E_MENU_INFO_INCORRECT_PARAM);
            }
            Menus pd = Menus.finder.where().eq("name",menus.name).findUnique();
            if(pd != null){
                throw new CodeException(ErrDefinition.E_MENU_INFO_INCORRECT_PARAM);
            }
            Ebean.save(menus);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_MENU_INFO_CREATE_FAILED);
        }
    }

    public Result read(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<Menus> exprList= Menus.finder.where();
            List<Menus> menusList;

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

            menusList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, menusList);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_MENU_INFO_READ_FAILED);
        }
    }

    public Result update() {
        try {
            Form<Menus> forms = formFactory.form(Menus.class).bindFromRequest();

            Menus menus = forms.get();
            if (null == menus.id || menus.id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_MENU_INFO_INCORRECT_PARAM);
            }
            Ebean.update(menus);
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_MENU_INFO_CREATE_FAILED);
        }
    }
}
