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
			chat.createTime=new Date();
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
	public Result delete(){
    	return delete(Chat.class,formFactory);
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

			ExpressionList<Chat> exprList = Chat.finder.where();
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
			
			Integer page = Integer.parseInt(pageStr);
			Integer num = Integer.parseInt(numStr);
			Integer follow = Integer.parseInt(followStr);
			// exprList.add(Expr.eq("follow", follow));
			exprList=exprList.eq("follow",follow);
			// exprList.add(Expr.eq("toId", session("userId")));
// 			if (settledStr != null && !settledStr.isEmpty()) {
// 				exprList.add(Expr.eq("isSettled", Boolean.parseBoolean(settledStr)));
// 			}

			chatList = exprList
					.setFirstRow((page-1)*num)
					.setMaxRows(num)
					.orderBy("createTime desc")
					.findList();

			int totalNum = exprList.findRowCount();
			int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

			return successList(totalNum, totalPage, chatList);
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