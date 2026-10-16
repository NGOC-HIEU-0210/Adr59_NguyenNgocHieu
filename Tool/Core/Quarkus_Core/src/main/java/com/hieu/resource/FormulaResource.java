package com.hieu.resource;

import com.hieu.dto.RecognizeResponse;
import com.hieu.service.PythonAiClient;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;

@Path("/api/formulas/recognize")
public class FormulaResource {

    private static final Logger log = Logger.getLogger(FormulaResource.class);

    @Inject
    PythonAiClient pythonAiClient;

    @POST
    @Blocking 
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recognize(
            @RestForm("image") FileUpload image,
            @RestForm("file") FileUpload file) {
        
        FileUpload upload = null;
        if (image != null && image.uploadedFile() != null) {
            upload = image;
        } else if (file != null && file.uploadedFile() != null) {
            upload = file;
        }

        if (upload == null || upload.uploadedFile() == null) {
            log.warn("Loi 400: Khong tim thay file anh trong form-data key 'image' hoac 'file'.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RecognizeResponse.error("Không nhận được file ảnh từ client. Vui lòng gửi multipart/form-data với key là 'image' hoặc 'file'."))
                    .build();
        }

        try {
            byte[] fileBytes = Files.readAllBytes(upload.filePath());
            String fileName = upload.fileName() != null ? upload.fileName() : "formula.png";
            String contentType = upload.contentType() != null ? upload.contentType() : "image/png";

            log.infof("Nhan file anh hop le: %s (size: %d bytes, type: %s)", fileName, fileBytes.length, contentType);

            RecognizeResponse result = pythonAiClient.recognizeFormulas(fileBytes, fileName, contentType);
            return Response.ok(result).build();
        } catch (IOException e) {
            log.errorf(e, "Loi doc file upload: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(RecognizeResponse.error("Lỗi đọc file: " + e.getMessage()))
                    .build();
        }
    }
}
