package controllers.device;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Events;
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
public class EventController extends BaseController {
    @Inject
    private FormFactory formFactory;

    public Result create() {
        return create(Events.class, formFactory);
    }
    public Result read(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Events> eventsList = null;
            String id = form.get("id");
            if (id != null && !id.isEmpty()) {
                Events events = Events.finder.byId(Integer.parseInt(id));
                if (events == null) {
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                eventsList = new ArrayList<Events>();
                eventsList.add(events);
                return successList(1, 1, eventsList);
            }

            ExpressionList<Events> exprList = Events.finder.where();
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
            String length=form.get("length");
            if (length != null && !length.isEmpty()) {
                exprList.add(Expr.eq("length", length));
            }
            String interval=form.get("interval");
            if (interval != null && !interval.isEmpty()) {
                exprList.add(Expr.eq("interval", interval));
            }
            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            if(starttime!=null&&!starttime.isEmpty()){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dates = sdf.parse(starttime);
                exprList.add(Expr.ge("time",dates));
            }
            if(endtime!=null&&!endtime.isEmpty()){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dates = sdf.parse(endtime);
                exprList.add(Expr.le("time",dates));
            }

            eventsList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("time desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, eventsList);
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
        return update(Events.class, formFactory);
    }
    public Result delete() {
        return delete(Events.class, formFactory);
    }
}
