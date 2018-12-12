package controllers.common;

import Sms.common.SmsManager;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import inceptors.common.HeaderAccessAction;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

@With(HeaderAccessAction.class)
public class SmsController extends Controller {
    
    public Result sendVerificationCode(String mobileNumber) {
        try {
            SmsManager.getInstance().sendVerification(mobileNumber);
        }
        catch (CodeException exc) {
            return BaseController.staticFailure(exc.getCode());
        }
        catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return BaseController.staticFailure(ErrDefinition.E_SMS_SEND_FAILED);
        }
        
        return BaseController.staticSuccess(null, null);
    }
    
    public Result verifyCode(String mobile, String code) {
        Boolean bRet = null;
        
        try {
            bRet = SmsManager.getInstance().verifyCode(mobile, code);            
        }
        catch (Throwable e) {
        }
        
        return BaseController.staticSuccess("result", bRet);
    }

}
