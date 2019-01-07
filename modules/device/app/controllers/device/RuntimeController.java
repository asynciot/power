package controllers.device;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Runtime;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2018/10/30.
 */
public class RuntimeController extends BaseController {
    @Inject
    private FormFactory formFactory;

    public Result create() {
        return create(Runtime.class, formFactory);
    }
    public Result read(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Runtime> runtimeList = null;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Runtime fault = Runtime.finder.byId(Integer.parseInt(id));
                if (fault == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                runtimeList = new ArrayList<Runtime>();
                runtimeList.add(fault);
                return successList(1, 1, runtimeList);
            }

            ExpressionList<Runtime> exprList = Runtime.finder.where();
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

            String device_id=form.get("device_id");
            if (device_id != null && !device_id.isEmpty()) {
                exprList.add(Expr.eq("device_id", device_id));
            }
            String type=form.get("type");
            if (type != null && !type.isEmpty()) {
                exprList.add(Expr.eq("type", type));
            }

            runtimeList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("t_update desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, runtimeList );
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
    public Result update() {
        return update(Runtime.class, formFactory);
    }
    public Result delete() {
        return delete(Runtime.class, formFactory);
    }
}
