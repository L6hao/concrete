package cc.coodex.concrete.attachments.server;


import cc.coodex.concrete.attachments.AttachmentEntityInfo;
import cc.coodex.concrete.attachments.AttachmentInfo;
import cc.coodex.util.Common;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static cc.coodex.concrete.attachments.AttachmentServiceHelper.ATTACHMENT_PROFILE;

/**
 * 附件上传的参考实现，基于jaxrs2.0;apache commons-fileupload
 * post /attachments/upload/byform/{clientId}/{tokenId}
 * Created by davidoff shen on 2016-12-13.
 */
@Path("attachments/upload/byform")
public class UploadByFormResource extends AbstractUploadResource {


    @Path("/{clientId}/{tokenId}")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    public void uploadByForm(
            @Suspended final AsyncResponse asyncResponse,
            @Context final HttpServletRequest request,
            @PathParam("clientId") final String clientId,
            @PathParam("tokenId") final String tokenId) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    ServletFileUpload uploadHandler = new ServletFileUpload(
                            new DiskFileItemFactory());
                    List<FileItem> items = uploadHandler.parseRequest(request);
                    List<AttachmentEntityInfo> result = new ArrayList<AttachmentEntityInfo>();
                    for (FileItem item : items) {
                        if (!item.isFormField()) {
                            if (!Common.isBlank(item.getName())) {
                                AttachmentInfo attachmentInfo = new AttachmentInfo();
                                attachmentInfo.setName(item.getName());
                                attachmentInfo.setOwner(clientId);
                                attachmentInfo.setSize(item.getSize());
                                attachmentInfo.setContentType(item.getContentType());
                                result.add(saveToRepo(clientId, tokenId, attachmentInfo, item.getInputStream()));
                            }
                        }
                    }
                    asyncResponse.resume(result);
                } catch (Throwable t) {
                    asyncResponse.resume(t);
                }
            }
        });
        t.setPriority(ATTACHMENT_PROFILE.getInt("upload.priority", 5));
        t.start();
    }

}
