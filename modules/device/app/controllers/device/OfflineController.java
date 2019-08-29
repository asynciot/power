package controllers.device;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.device.DeviceInfo;
import models.device.Offline;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import play.libs.Json;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lengxia on 2018/12/9.
 */
public class OfflineController extends BaseController {

    @Inject
    FormFactory formFactory;

	public Result logoutlist(){
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			String pageStr = form.get("page");
			String numStr = form.get("num");
			String starttime = form.get("starttime");
			String endtime = form.get("endtime");
			if (null == pageStr || pageStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			
			if (null == numStr || numStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			int page = Integer.parseInt(pageStr);
			int num = Integer.parseInt(numStr);
			String sql="SELECT device_name,imei,ladder.`device_info`.id,count(1) as counter FROM ladder.`offline` left join ladder.`device_info` on ladder.`offline`.device_id=ladder.`device_info`.id where  ";
			if(starttime!=null&&!starttime.isEmpty()){
				sql=sql+"t_logout>'"+starttime+"' ";
			}
			if(endtime!=null&&!endtime.isEmpty()){
				sql=sql+"AND t_logout<'"+endtime+"' ";
			}
			String sql2=sql+"group by ladder.`device_info`.id;";
			Logger.info("1----------------------------");
			List<SqlRow> orderList=Ebean.createSqlQuery(sql2)
					.findList();
			int totalNum = orderList.size();
			sql=sql+"group by ladder.`device_info`.id order by counter desc limit "+(page-1)*num+","+num;
			orderList=Ebean.createSqlQuery(sql)
					.findList();
			int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
			return successList(totalNum,totalPage,orderList);
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
	
	public Result single(){
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			String starttime = form.get("starttime");
			String endtime = form.get("endtime");
			String id = form.get("id");

			String sql="SELECT t_logout FROM ladder.`offline` where device_id='"+id+"' ";
			
			if(starttime!=null&&!starttime.isEmpty()){
			    sql=sql+"AND t_logout>'"+starttime+"' ";
			}
			if(endtime!=null&&!endtime.isEmpty()){
				sql=sql+"AND t_logout<'"+endtime+"' ";
			}
			sql=sql+"order by id desc";
			
			List<SqlRow> orderList=Ebean.createSqlQuery(sql)
										.findList();
			return successList(orderList);
			}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_READ_FAILED);
		}
	}

	public Result offlinelist(){
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			ExpressionList<Offline> exprList= Offline.finder.where();
			String pageStr = form.get("page");
			String numStr = form.get("num");
			String starttime = form.get("starttime");
			String endtime = form.get("endtime");
			if (null == pageStr || pageStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}

			if (null == numStr || numStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			int page = Integer.parseInt(pageStr);
			int num = Integer.parseInt(numStr);
			if(starttime!=null&&!starttime.isEmpty()&&endtime!=null&&!endtime.isEmpty()){
				exprList = Offline.finder.where().gt("t_logout",starttime).lt("t_logout",endtime);
			}

			List<Offline> offlineList = exprList
					.setFirstRow((page-1)*num)
					.setMaxRows(num)
                    .orderBy("id")
					.findList();
			int totalNum = exprList.findRowCount();
			int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
			return successList(totalNum,totalPage,offlineList);
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

	public Result deviceOffline(){
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			ExpressionList<Offline> exprList= Offline.finder.where();
			String starttime = form.get("starttime");
			String endtime = form.get("endtime");
			String id = form.get("id");
			String device_name = form.get("device_name");

			if(starttime!=null&&!starttime.isEmpty()&&endtime!=null&&!endtime.isEmpty()){
				exprList = Offline.finder.where().gt("t_logout",starttime).lt("t_logout",endtime);
			}
			if(id!=null && !id.isEmpty()){
				exprList = exprList.in("device_id",id);
			}
			if(device_name!=null && !device_name.isEmpty()){
				DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("device_name",device_name).findUnique();
				if (deviceInfo != null){
					exprList = exprList.in("device_id",deviceInfo.id);
				}
			}

			List<Offline> offlineList= exprList
					.findList();
			return successList(offlineList);
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_READ_FAILED);
		}
	}
}
