package controllers.common;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.common.Chat;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import javax.security.auth.login.AccountException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.avaje.ebean.SqlRow;




public class ChatController extends BaseController {
    @Inject
    private FormFactory formFactory;

    public Result create(){
    	try {
			DynamicForm form = formFactory.form().bindFromRequest();
			Chat chat=new Chat();
			String title=form.get("title");
			String content=form.get("content");
			String follow=form.get("follow");
			String type=form.get("type");
			String fromId=form.get("fromId");
			String info=form.get("info");
			if(follow==null||follow.isEmpty()||fromId==null||fromId.isEmpty()||title==null||title.isEmpty()){
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			chat.content=content;
			chat.follow=Integer.parseInt(follow);
			chat.title=title;
			chat.fromId=fromId;
			chat.type=Integer.parseInt(type);
			chat.info=info;
			chat.create_time=new Date();
			Ebean.save(chat);
			return success();
		} 	catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_CREATE_FAILED);
		}


	}
// 	public Result delete(){
//     	return delete(Chat.class,formFactory);
// 	}
	public Result delete() {
	    try {
	        DynamicForm form = formFactory.form().bindFromRequest();
	        String id = form.get("id");
	        Chat chat= Chat.finder.where()
	                .eq("id",id)
	                .findUnique();
	        if (chat == null ) {
	            throw new CodeException(ErrDefinition.E_FOLLOW_INFO_INCORRECT_PARAM);
	        }
	        Ebean.delete(chat);
	        return success();
	    }
	    catch (CodeException ce) {
	        Logger.error(ce.getMessage());
	        return failure(ce.getCode());
	    }
	    catch (Throwable e) {
	        e.printStackTrace();
	        Logger.error(e.getMessage());
	        return failure(ErrDefinition.E_FOLLOW_INFO_DELETE_FAILED);
	    }
	}
	public Result read() {
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			List<Chat> chatList = null;
			String id = form.get("id");
			if (id != null && !id.isEmpty()) {
				Chat chat = Chat.finder.byId(Integer.parseInt(id));
				if (chat == null) {
					throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
				}

				chatList = new ArrayList<Chat>();
				chatList.add(chat);
				return successList(1, 1, chatList);
			}

			String pageStr = form.get("page");
			String numStr = form.get("num");
			String followStr = form.get("follow");
			if (null == pageStr || pageStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}

			if (null == numStr || numStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			
			if (null == followStr || followStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}			
			int page = Integer.parseInt(pageStr);
			int num = Integer.parseInt(numStr);
			int follow = Integer.parseInt(followStr);
			
			String sql="SELECT ladder.`chat`.id,content,follow,username,from_id,nickname,ladder.`chat`.create_time,portrait FROM ladder.`chat` left join ladder.`account` on ladder.`chat`.from_id=ladder.`account`.id ";
			sql=sql+"WHERE follow='"+follow+"' ";
			sql=sql+"order by create_time desc limit "+(page-1)*num+","+num;
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
			return failure(ErrDefinition.E_MESSAGE_READ_FAILED);
		}
	}
// 	public Result readCount() {
// 		try {
// 			int notSettledCount = Chat.finder
// 					.where()
// 					.eq("toId", session("userId"))
// 					.eq("isSettled", false)
// 					.findRowCount();
// 
// 			int settledCount = Chat.finder
// 					.where()
// 					.eq("toId", session("userId"))
// 					.eq("isSettled", true)
// 					.findRowCount();
// 			
// 			ObjectNode node = Json.newObject();
// 			node.put("all", String.format("%d", notSettledCount+settledCount));
// 			node.put("done", String.format("%d", settledCount));
// 			node.put("unread", String.format("%d", notSettledCount));
// 			return success("data", node);
// 		}
//         catch (Throwable e) {
//             e.printStackTrace();
//             Logger.error(e.getMessage());
//             return failure(ErrDefinition.E_MESSAGE_READ_FAILED);
//         }        		
// 	}
// 	public Result settle() {
// 		try {
// 			DynamicForm form = formFactory.form().bindFromRequest();
// 
// 			String id = form.get("id");
// 
// 			if (id == null || id.isEmpty()) {
// 				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
// 			}
// 
// 			Chat chat = Chat.finder.byId(Integer.parseInt(id));
// 
// 			if (chat == null) {
// 				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
// 			}
// 
// 			chat.isSettled = true;
// 			Ebean.save(chat);
// 
// 			return success();
// 		}
// 		catch (CodeException ce) {
// 			Logger.error(ce.getMessage());
// 			return failure(ce.getCode());
// 		}
// 		catch (Throwable e) {
// 			e.printStackTrace();
// 			Logger.error(e.getMessage());
// 			return failure(ErrDefinition.E_MESSAGE_UPDATE_FAILED);
// 		}
// 	}



}