package controllers.device;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import controllers.common.BaseController;
import controllers.common.CodeException;
import controllers.common.ErrDefinition;
import models.device.Binaries;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2018/11/30.
 */
public class BinarysController  extends BaseController {

    @Inject
    private FormFactory formFactory;
    public Result upload() throws IOException {
        try{
            Http.MultipartFormData body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart filepart = body.getFile("file");
            if (filepart != null) {
                String fileName = filepart.getFilename().split("\\.")[0];
                int count= Binaries.finder.where().eq("name",fileName).eq("type","firmware").findRowCount();
                if(count>0){
                    throw  new CodeException(ErrDefinition.E_COMMOND_REGISTER_ALREADY_EXIST);
                }
                String contentType = filepart.getContentType();
                File file = (File) filepart.getFile();//获取到默认上传保存的完整文件路径，这只是个临时文件
                Binaries binaries = new Binaries();
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int n;
                while ((n = fis.read(b)) != -1)
                {
                    bos.write(b, 0, n);
                }
                fis.close();
                bos.close();
                binaries.data = bos.toByteArray();
                binaries.t_create = new Date();
                binaries.name=fileName;
                binaries.type="firmware";
                binaries.save();
                return success("binaries", binaries.id);
            } else {
                return failure(ErrDefinition.E_BINARYS_UPLOAD_FAILD );
            }
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
    public Result delete(){
        return delete(Binaries.class,formFactory);
    }
    public Result read() {
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            List<Binaries> binariesList = null;
            String id = form.get("id");

            if (id != null && !id.isEmpty()) {
                Binaries binaries = Binaries.finder.byId(Integer.parseInt(id));
                if(binaries == null){
                    throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
                }
                binariesList = new ArrayList<Binaries>();
                binariesList.add(binaries);
                return successList(1, 1, binariesList);
            }

            ExpressionList<Binaries> exprList = Binaries.finder.where();
            String pageStr = form.get("page");
            String numStr = form.get("num");

            if (null == pageStr || pageStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            if (null == numStr || numStr.isEmpty()) {
                throw new CodeException(ErrDefinition.E_COMMON_INCORRECT_PARAM);
            }

            Integer page = Integer.parseInt(pageStr);
            Integer num = Integer.parseInt(numStr);
            String type = form.get("type");
            if(type!=null&&!type.isEmpty()){
                exprList.add(Expr.eq("type", type));
            }

            binariesList = exprList
                    .setDisableLazyLoading(true)
                    .select("name")
                    .setFirstRow((page-1)*num)
                    .setMaxRows(num)
                    .findList();

            int totalNum = exprList.findRowCount();
            int totalPage = totalNum % num == 0 ? totalNum / num : totalNum / num + 1;

            return successList(totalNum, totalPage, binariesList);
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


}
