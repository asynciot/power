package controllers.device;

import controllers.common.BaseController;
import models.device.Logs;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * Created by lengxia on 2018/10/30.
 */
public class LogController extends BaseController {
    @Inject
    private FormFactory formFactory;

    public Result create() {
        return create(Logs.class, formFactory);
    }
    public Result read(){
        return read(Logs.class, formFactory);
    }
    public Result update() {
        return update(Logs.class, formFactory);
    }
    public Result delete() {
        return delete(Logs.class, formFactory);
    }
}
