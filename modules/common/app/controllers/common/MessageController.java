package controllers.common;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.common.Message;
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

public class MessageController extends BaseController {
    @Inject
    private FormFactory formFactory;

    public Result create(){
    	try {
			DynamicForm form = formFactory.form().bindFromRequest();
			Message message=new Message();
			String title=form.get("title");
			String content=form.get("content");
			String type=form.get("type");
			String toId=form.get("toId");
			String fromId=form.get("fromId");
			String info=form.get("info");
			if(toId==null||toId.isEmpty()||fromId==null||fromId.isEmpty()||title==null||title.isEmpty()){
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			message.content=content;
			message.title=title;
			message.fromId=fromId;
			message.toId=toId;
			message.type=Integer.parseInt(type);
			message.info=info;
			message.createTime=new Date();
			message.isSettled=false;
			Ebean.save(message);
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
    	return delete(Message.class,formFactory);
	}
	public Result read() {
		try {
			DynamicForm form = formFactory.form().bindFromRequest();
			List<Message> messageList = null;
			String id = form.get("id");
			if (id != null && !id.isEmpty()) {
				Message message = Message.finder.byId(Integer.parseInt(id));
				if (message == null) {
					throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
				}

				if (message.isSettled == false) {
					message.isSettled = true;
					Ebean.save(message);
				}

				messageList = new ArrayList<Message>();
				messageList.add(message);
				return successList(1, 1, messageList);
			}

			ExpressionList<Message> exprList = Message.finder.where();
			String pageStr = form.get("page");
			String numStr = form.get("num");
			String settledStr = form.get("done");

			if (null == pageStr || pageStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}

			if (null == numStr || numStr.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}

			Integer page = Integer.parseInt(pageStr);
			Integer num = Integer.parseInt(numStr);

			exprList.add(Expr.eq("toId", session("userId")));

			if (settledStr != null && !settledStr.isEmpty()) {
				exprList.add(Expr.eq("isSettled", Boolean.parseBoolean(settledStr)));
			}

			messageList = exprList
					.setFirstRow((page-1)*num)
					.setMaxRows(num)
					.orderBy("createTime desc")
					.findList();

			int totalNum = exprList.findRowCount();
			int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

			return successList(totalNum, totalPage, messageList);
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
	public Result readCount() {
		try {
			int notSettledCount = Message.finder
					.where()
					.eq("toId", session("userId"))
					.eq("isSettled", false)
					.findRowCount();

			int settledCount = Message.finder
					.where()
					.eq("toId", session("userId"))
					.eq("isSettled", true)
					.findRowCount();
			
			ObjectNode node = Json.newObject();
			node.put("all", String.format("%d", notSettledCount+settledCount));
			node.put("done", String.format("%d", settledCount));
			node.put("unread", String.format("%d", notSettledCount));
			return success("data", node);
		}
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_MESSAGE_READ_FAILED);
        }        		
	}
	public Result settle() {
		try {
			DynamicForm form = formFactory.form().bindFromRequest();

			String id = form.get("id");

			if (id == null || id.isEmpty()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}

			Message message = Message.finder.byId(Integer.parseInt(id));

			if (message == null) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}

			message.isSettled = true;
			Ebean.save(message);

			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_MESSAGE_UPDATE_FAILED);
		}
	}



}