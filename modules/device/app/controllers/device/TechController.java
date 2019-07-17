package controllers.device;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Tech;
import play.Logger;
import play.core.j.JavaResultExtractor;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Result;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lengxia on 2018/10/30.
 */
public class TechController extends BaseController {
    @Inject
    private FormFactory formFactory;

    @Inject
    private Materializer mat;

    public Result read(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            ExpressionList<Tech> exprList = Tech.finder.where();
            List<Tech> techList = new ArrayList<Tech>();
            String pageStr = form.get("page");
            String numStr = form.get("num");
            String code = form.get("code");
            String search_info = form.get("search_info");
            if (pageStr==null||pageStr.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (numStr==null||numStr.isEmpty()){
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            if (code!=null&&!code.isEmpty()){
                exprList=exprList.contains("code",code);
            }
            if (search_info!=null&&!search_info.isEmpty()){
                exprList=exprList.contains("name",search_info);
                if (exprList.findRowCount()<1){
                    exprList=exprList.contains("reason",search_info);
                    if (exprList.findRowCount()<1){
                        exprList=exprList.contains("answer",search_info);
                    }
                }
            }
            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);
            techList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();
            return successList(techList);
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

}
