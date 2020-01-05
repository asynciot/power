package controllers.device;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Cellocation;
import models.device.DeviceInfo;
import models.device.Devices;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import sun.rmi.runtime.Log;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class CellocationController extends BaseController{
    @Inject
    private FormFactory formFactory;

    public Result create(){
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            String lat = form.get("lat");
            String lon = form.get("lon");
            String device_id = form.get("device_id");

            String url = "http://api.cellocation.com:81/regeo/?lat="+lat+"&lon="+lon;
            Map<String, Object> result = new HashMap<>();

            CompletionStage<JsonNode> jsonPromise = WS.url(url)
                    .setContentType("application/json")
                    .get()
                    .thenApply(WSResponse::asJson);
            JsonNode retVal = jsonPromise.toCompletableFuture().get();
            if (retVal.get("errcode").asInt()==0){
                Cellocation cellocation = new Cellocation();
                cellocation.address = retVal.get("address").asText();

                cellocation.lat = Double.valueOf(lat);
                cellocation.lon = Double.valueOf(lon);
                cellocation.cell_mnc = 2;
                cellocation.cell_mcc = 460;
                cellocation.cell_lac = 0;
                cellocation.cell_cid = 0;
                cellocation.radius = 0.0;

                Ebean.save(cellocation);
                cellocation = Cellocation.finder.where().eq("address",retVal.get("address").asText()).findUnique();
                DeviceInfo deviceInfo = DeviceInfo.finder.where().eq("id",device_id).findUnique();
                deviceInfo.cellocation_id = cellocation.id;
                Ebean.save(deviceInfo);
            }
            return success();
        }
        catch (Throwable e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            return failure(ErrDefinition.E_COMMON_CREATE_FAILED);
        }
    }
}
