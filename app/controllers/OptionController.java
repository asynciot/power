package controllers;

import play.mvc.Controller;
import play.mvc.Result;

//解决跨域问题
public class OptionController extends Controller {
    public Result OptionMethod(String name) {
        String header = request().getHeader("Origin");
        header = (header == null ? "*" : header);
        response().setHeader("Access-Control-Allow-Origin", header);
        response().setHeader("Access-Control-Allow-Credentials", "true");
        response().setHeader("Access-Control-Allow-Headers", "Content-Type");
        response().setHeader("Access-Control-Allow-Methods", "POST,GET,PUT,DELETE,OPTIONS");
        response().setHeader("Access-Controll-Max-Age", "60000");
        //Logger.info("OK:" + name);
        return ok();
    }
}
