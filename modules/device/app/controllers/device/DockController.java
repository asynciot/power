package controllers.device;

import controllers.common.BaseController;
import models.device.Docks;
import play.data.FormFactory;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * Created by lengxia on 2018/10/30.
 */
public class DockController extends BaseController {
    @Inject
    private FormFactory formFactory;

    public Result create() {
        return create(Docks.class, formFactory);
    }
    public Result read(){
        return read(Docks.class, formFactory);
    }
    public Result update() {
        return update(Docks.class, formFactory);
    }
    public Result delete() {
        return delete(Docks.class, formFactory);
    }
}
