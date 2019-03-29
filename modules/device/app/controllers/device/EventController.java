package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.device.Events;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Result;
import play.libs.Json;

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
            String device_id=form.get("device_id");
            ExpressionList<Events> exprList = Events.finder.where().eq("device_id",device_id);
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

            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(starttime!=null&&!starttime.isEmpty()){
                Date st = sdf.parse(starttime);
                exprList.add(Expr.ge("time",st));
            }
            if(endtime!=null&&!endtime.isEmpty()){
                Date ed = sdf.parse(endtime);
                exprList.add(Expr.le("time",ed));
            }
            String length=form.get("length");
            if (length != null && !length.isEmpty()) {
                exprList.add(Expr.eq("length", length));
            }
            String interval=form.get("interval");
            if (interval != null && !interval.isEmpty()) {
                exprList.add(Expr.eq("interval", interval));
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
    public Result readCountEvent(){
        try{
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Events> eventsList = null;

            String starttime = form.get("starttime");
            String endtime = form.get("endtime");
            if (null == starttime || starttime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (null == endtime || endtime.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long strt = sdf.parse(starttime).getTime();
            long edt= strt+86400000;
            Date st = sdf.parse(sdf.format(strt));
            Date ed = sdf.parse(sdf.format(edt));

            int monday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int tuesday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int wensday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int thursday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int friday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int saturday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();
            strt = edt;
            edt= strt+86400000;
            st = sdf.parse(sdf.format(strt));
            ed = sdf.parse(sdf.format(edt));

            int sunday =Events.finder
                    .where()
                    .ge("time", st)
                    .le("time", ed)
                    .findRowCount();

            ObjectNode node = Json.newObject();
            node.put("monday", String.format("%d", monday));
            node.put("tuesday", String.format("%d", tuesday));
            node.put("wensday", String.format("%d", wensday));
            node.put("thursday", String.format("%d", thursday));
            node.put("friday", String.format("%d", friday));
            node.put("saturday", String.format("%d", saturday));
            node.put("sunday", String.format("%d", sunday));

            return success("data",node);
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

	public Result activedoor(){
		DynamicForm form = formFactory.form().bindFromRequest();
	    String sql="SELECT device_id,device_name,count(1) as counter FROM ladder.`events` inner join ladder.`device_info` on ladder.`events`.device_id=ladder.`device_info`.id ";
		String starttime = form.get("starttime");
		String endtime = form.get("endtime");
		if(starttime!=null&&!starttime.isEmpty()){
		    sql=sql+"WHERE time>'"+starttime+"' ";
		}
		if(endtime!=null&&!endtime.isEmpty()){
		    if(starttime!=null&&!starttime.isEmpty()){
				sql=sql+"AND time>'"+endtime+"' ";
			}
			else{
				sql=sql+"WHERE time<'"+endtime+"' ";
			}
		}
		sql=sql+"group by device_id order by count(device_id) desc limit 10";
	    List<SqlRow> orderList=Ebean.createSqlQuery(sql).findList();

	    Logger.info(orderList.size()+"");
	    return successList(orderList);
		
// 		ObjectNode node = Json.newObject();
// 		node.put("sql", sql);		
// 		return success("data",node);
	}

    public Result update() {
        return update(Events.class, formFactory);
    }
    public Result delete() {
        return delete(Events.class, formFactory);
    }

}
