package controllers.account;

import Sms.common.SmsManager;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.common.CodeException;
import controllers.common.CodeGenerator;
import controllers.common.ErrDefinition;
import controllers.common.XDomainController;
import inceptors.common.Secured;
import models.account.Account;
import models.common.Message;
import models.common.SmsRecord;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by lengxia on 2018/10/30.
 */
public class AccountController extends XDomainController {

    @Inject
    public FormFactory formFactory;
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<Account> form = formFactory.form(Account.class);
        try {
            String userId = session("userId");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Account adminAccount = Account.finder.byId(userId);
            if (adminAccount == null||!adminAccount.username.equals("admin")) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }
            Account newAccount = form.bindFromRequest().get();
            if (Account.finder.where().eq("username", newAccount.username).findRowCount() != 0) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_ALREADY_EXIST);
            }
            if(Account.finder.where().eq("mobile",newAccount.mobile).findRowCount()!=0){
                throw new CodeException(ErrDefinition.E_ACCOUNT_ALREADY_BINDED);
            }

            if (newAccount.password == null || newAccount.password.isEmpty()) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_NO_PASSWORD);
            }

            if (newAccount.password != null && !newAccount.password.isEmpty()) {
                newAccount.password = CodeGenerator.generateMD5(newAccount.password);
            }

            newAccount.id = CodeGenerator.generateShortUUId();
            newAccount.createTime = new Date();
            Ebean.save(newAccount);

            return success("id", newAccount.id);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_CREATE_FAILED);
        }
    }
    public Result login() {
        DynamicForm form=formFactory.form().bindFromRequest();
        try {
            session().clear();

            String username = form.get("username");
            String password = form.get("password");


            if (null == username || null == password) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }

            Account account = Account.finder.where().eq("username", username).findUnique();
            if (account == null) {
                account = Account.finder.where().eq("mobile", username).findUnique();
            }
            if(account==null){
                throw new CodeException(ErrDefinition.E_ACCOUNT_NOT_FOUND);

            }
            if (account.password.compareTo(CodeGenerator.generateMD5(password)) != 0) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_PASSWORD_MISMATCH);
            }
            session("userId", account.id);
            ObjectNode node = Json.newObject();
            node.put("id", account.id);
            node.put("username", account.username);
            node.put("mobile", account.mobile);
            node.put("nickname",account.nickname);
            return success("account", node);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_LOGIN_FAILED);
        }
    }
    public Result logout() {
        session().clear();
        return success(null, null);
    }
    @Security.Authenticated(Secured.class)
    public Result update() {
        Form<Account> form = formFactory.form(Account.class).bindFromRequest();

        try {
            if (form.hasErrors()) {
                Logger.info(form.errorsAsJson()+"");
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }
            String userId = session("userId");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Account tmp = form.get();
            Account account = Account.finder.byId(tmp.id);
            if (account == null) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }

            if (tmp.nickname != null) {
                account.nickname = tmp.nickname;
            }
            if (tmp.mobile != null) {
                account.mobile = tmp.mobile;
            }
            if (tmp.sex != null) {
                account.sex = tmp.sex;
            }
            if (tmp.birthday != null) {
                account.birthday = tmp.birthday;
            }
            if (tmp.profession != null) {
                account.profession = tmp.profession;
            }
            if (tmp.email != null) {
                account.email = tmp.email;
            }
            if (tmp.introduction != null) {
                account.introduction = tmp.introduction;
            }
            Ebean.update(account);
            return success(null, null);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_UPDATE_FAILED);
        }

    }
    @Security.Authenticated(Secured.class)
    public Result read() {
        try {

            DynamicForm form = formFactory.form().bindFromRequest();

            String userId = session("userId");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Account adminAccount = Account.finder.byId(userId);

            List<Account> accountList = null;
            userId = form.get("id");
            if (userId != null && !userId.isEmpty()) {
                Account account = Account.finder.byId(userId);
                if (null == account) {
                    throw new CodeException(ErrDefinition.E_ACCOUNT_NOT_FOUND);
                }
                accountList = new ArrayList<Account>();
                accountList.add(account);
                return successList(1, 1, accountList);
            }


            if (adminAccount == null||!adminAccount.username.equals("admin")) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }

            ExpressionList<Account> exprList = Account.finder.where().notIn("username","admin");
            String pageStr = form.get("page");
            String numStr = form.get("num");

            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }

            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);

            String username = form.get("username");
            if (username != null && !username.isEmpty()) {
                exprList.add(Expr.contains("username", username));
            }
            accountList = exprList
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .orderBy("createTime desc")
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, accountList);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_READ_FAILED);
        }
    }
    @Security.Authenticated(Secured.class)
    public Result delete() {
        try {
            String userId = session("userId");
            if (null == userId) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            Account adminAccount = Account.finder.byId(userId);
            if (adminAccount == null||!adminAccount.username.equals("admin")) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }
            DynamicForm form = formFactory.form().bindFromRequest();
            String id = form.get("id");
            if (id == null || id.isEmpty()) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }
            Account accountTodelete = Account.finder.byId(id);
            if (accountTodelete == null) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }
            Ebean.delete(accountTodelete);

            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_DELETE_FAILED);
        }
    }
    public Result register() {
        Ebean.beginTransaction();
        try {
            Form<Account> form = formFactory.form(Account.class).bindFromRequest();

            if (form.hasErrors()) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }

            Calendar nowTime = Calendar.getInstance();
            nowTime.setTime(new Date());
            Calendar oneMinAgo = Calendar.getInstance();
            oneMinAgo.setTime(new Date());
            oneMinAgo.add(Calendar.MINUTE, -5);
            Account account = form.get();
            Account existAccount = Account.finder.where()
                    .eq("username", account.username)
                    .findUnique();
            if(existAccount==null){
                existAccount=Account.finder.where().eq("mobile",account.mobile).findUnique();
            }
            if (existAccount != null) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_ALREADY_EXIST);
            }
            SmsRecord record = SmsRecord.find.where()
                    .eq("mobile", account.mobile)
                    .eq("code", account.verifyCode)
                    .between("timestamp", oneMinAgo.getTime(), nowTime.getTime())
                    .setMaxRows(1)
                    .orderBy("timestamp desc")
                    .findUnique();

            if (record == null) {
                throw new CodeException(ErrDefinition.E_SMS_VERIFY_FAILED);
            }

            account.password = CodeGenerator.generateMD5(account.password);
            account.id = CodeGenerator.generateShortUUId();
            account.createTime = new Date();
            Ebean.save(account);
            createWelcomeMsg(account.id);
            Ebean.commitTransaction();
            return success("id", account.id);
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_CREATE_FAILED);
        }
        finally {
            Ebean.endTransaction();
        }
    }
    @Security.Authenticated(Secured.class)
    public Result password() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();

            String userId = session("userId");

            if (userId == null || userId.isEmpty()) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_UNAUTHENTICATED);
            }

            String password = form.get("password");
            if (password == null || password.isEmpty()){
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }

            String oldPassword = form.get("oldPassword");
            if (oldPassword == null || oldPassword.isEmpty()){
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }

            Account account = Account.finder.byId(session("userId"));


            if (account.password.compareTo(CodeGenerator.generateMD5(oldPassword)) != 0) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_PASSWORD_MISMATCH);
            }

            account.password = CodeGenerator.generateMD5(password);

            Ebean.update(account);
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_UPDATE_FAILED);
        }
    }
    public Result retrieve(){
        DynamicForm form=formFactory.form().bindFromRequest();
        try {
            String mobile = form.get("mobile");
            String code = form.get("verifyCode");
            String newpassword=form.get("newpassword");
            if(mobile==null||code==null||newpassword==null){
                throw new CodeException(ErrDefinition.E_ACCOUNT_INCORRECT_PARAM);
            }
            if(SmsManager.getInstance().verifyCode(mobile, code)==false){
                throw new CodeException(ErrDefinition.E_SMS_VERIFY_FAILED);
            }
            Account account=Account.finder.where().eq("mobile", mobile).findUnique();;
            if (account==null) {
                throw new CodeException(ErrDefinition.E_ACCOUNT_NOT_FOUND);
            }
            account.password=CodeGenerator.generateMD5(newpassword);
            Ebean.update(account);
            return success();
        }
        catch (CodeException ce) {
            Logger.error(ce.getMessage());
            return failure(ce.getCode());
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_ACCOUNT_LOGIN_FAILED);
        }

    }
    private void createWelcomeMsg(String toId) {
        //create notification message
        Message message = new Message();
        message.title = "注册消息";
        message.content = "欢迎您加入宁波申菱电梯物联网平台";
        message.type = 1;
        message.fromId = "-1";
        message.toId = toId;
        message.createTime = new Date();
        Ebean.save(message);
    }
}
