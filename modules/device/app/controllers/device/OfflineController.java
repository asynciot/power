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
			Integer page = Integer.parseInt(pageStr);
			Integer num = Integer.parseInt(numStr);
			String sql="SELECT device_name,imei,ladder.`device_info`.id,count(1) as counter FROM ladder.`offline` left join ladder.`device_info` on ladder.`offline`.device_id=ladder.`device_info`.id where ladder.`offline`.device_id>'0' ";
			
			if(starttime!=null&&!starttime.isEmpty()){
			    sql=sql+"AND t_logout>'"+starttime+"' ";
			}
			if(endtime!=null&&!endtime.isEmpty()){
				sql=sql+"AND t_logout<'"+endtime+"' ";
			}
			
			sql=sql+"group by ladder.`device_info`.id order by counter desc limit "+(page-1)*num+","+num;
			List<SqlRow> orderList=Ebean.createSqlQuery(sql)
										.findList();				
			Logger.info(orderList.size()+"");
			return successList(orderList);
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

			String sql="SELECT t_logout FROM ladder.`offline` where ladder.`offline`.device_id='"+id+"' ";
			
			if(starttime!=null&&!starttime.isEmpty()){
			    sql=sql+"AND t_logout>'"+starttime+"' ";
			}
			if(endtime!=null&&!endtime.isEmpty()){
				sql=sql+"AND t_logout<'"+endtime+"' ";
			}
			sql=sql+"order by t_logout desc";
			
			List<SqlRow> orderList=Ebean.createSqlQuery(sql)
										.findList();
			Logger.info(orderList.size()+"");
			return successList(orderList);
			}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_READ_FAILED);
		}
	}
}
