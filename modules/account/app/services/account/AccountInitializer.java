package services.account;

import com.avaje.ebean.Ebean;
import controllers.common.CodeGenerator;
import models.account.Account;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AccountInitializer {

    @Inject
    public AccountInitializer() {
        
        if (Ebean.find(Account.class).findRowCount() == 0) {
            try {
                Account superAdmin = new Account();
                superAdmin.id = CodeGenerator.generateUUId();
                superAdmin.username = "admin";
                superAdmin.password = CodeGenerator.generateMD5("adminadmin");
                superAdmin.mobile="18018592775";
                superAdmin.augroup=1;
                superAdmin.maxfollow=100000;
                Ebean.save(superAdmin);
                Logger.info("create super admin successfully.");
            }
            catch (Throwable e) {
                e.printStackTrace();
                Logger.error("fail to create super admin.");
            }
        }
    }
}
