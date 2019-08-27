package controllers.common;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import inceptors.common.Secured;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import play.mvc.Security;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Security.Authenticated(Secured.class)
public class BaseController extends XDomainController {
	    
	public <T> Result create(Class<T> modelClass, FormFactory formFactory) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			
			Form<T> form = formFactory.form(modelClass).bindFromRequest();
			
			if (form.hasErrors()) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			T obj = form.get();
			Ebean.save(obj);
			
			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_CREATE_FAILED);
		}				        
	}
	
	public <T> Result read(Class<T> modelClass, FormFactory formFactory) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			List<T> list;
			DynamicForm form = formFactory.form().bindFromRequest();
			
			String id = form.get("id");			
			if (id != null && !id.isEmpty()) {
				T obj;
				Field idField = modelClass.getDeclaredField("id");
				if (idField.getType().isAssignableFrom(Integer.class)) {
					obj = Ebean.find(modelClass, Integer.parseInt(id));
				}
				else {
					obj = Ebean.find(modelClass, id);
				}
				
				if (obj == null) {
					throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
				}
				
				list = new ArrayList<>();
				list.add(obj);
				addMoreInfo(list);
				return successList(1, 1, list);
			}
			
            ExpressionList<T> exprList = Ebean.find(modelClass).where();
            String pageStr = form.get("page");
            String numStr = form.get("num");
           
            if (null == pageStr || pageStr.isEmpty()) {
            	throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
           
            if (null == numStr || numStr.isEmpty()) {
            	throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
           
            int page = Integer.parseInt(pageStr);
            int num = Integer.parseInt(numStr);
           
            addExprFilter(exprList, formFactory);
            com.avaje.ebean.Query<T> query = exprList
            		.setFirstRow((page-1)*num)
            		.setMaxRows(num);
            
            Field createTimeField;
            try {
            	createTimeField = modelClass.getDeclaredField("createTime");
            }
            catch (Throwable e1) {
            	createTimeField = null;
            }
            
            if (null != createTimeField) {
            	String order = form.get("order");
            	if (order != null) {
                	list = query.orderBy("createTime " + order).findList();            		
            	}
            	else {
                	list = query.orderBy("createTime desc").findList();            		
            	}
            }
            else {
                list = query.findList();            	
            }
                   
            addMoreInfo(list);
            int totalNum = exprList.findRowCount();            
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;
                   
            return successList(totalNum, totalPage, list);			
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

	protected <T> void addExprFilter(ExpressionList<T> exprList, FormFactory formFactory) {

	}

	protected <T> void addMoreInfo(List<T> list) {

	}

	public <T> Result update(Class<T> modelClass, FormFactory formFactory) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			
            Form<T> form = formFactory.form(modelClass).bindFromRequest();
            
            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
            
            T tmp = form.get();
            
			Field idField = modelClass.getDeclaredField("id");
			T obj = Ebean.find(modelClass, idField.get(tmp));
            			
            if (obj == null) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }
			
            Field[] fields = modelClass.getDeclaredFields();
            
            for (Field field : fields) {
            	if (field.getName().compareTo("id") == 0) {
            		continue;
            	}
                field.setAccessible(true);
            	if (field.get(tmp) != null) {
            		field.set(obj, field.get(tmp));
            	}
            }
            
            Ebean.update(obj);
            
            return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_UPDATE_FAILED);
		}				        
	}
	
	public <T> Result delete(Class<T> modelClass, FormFactory formFactory) {
		try {
			if (!Model.class.isAssignableFrom(modelClass)) {
				throw new CodeException(ErrDefinition.E_CLASS_NOT_SUPPORT);
			}
			
			DynamicForm form = formFactory.form().bindFromRequest();
			
			String id = form.get("id");
			T obj = Ebean.find(modelClass, id);
			
			if (obj == null) {
				throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
			}
			
			Ebean.delete(obj);
			return success();
		}
		catch (CodeException ce) {
			Logger.error(ce.getMessage());
			return failure(ce.getCode());
		}
		catch (Throwable e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			return failure(ErrDefinition.E_COMMON_DELETE_FAILED);
		}				        
	}
}
