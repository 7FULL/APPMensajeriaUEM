package com.appmsg.appmensajeriauem;

import com.google.gson.JsonObject;
import org.bson.types.ObjectId;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "fileUploadServlet", value = "/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 50,       // 50MB por archivo
    maxRequestSize = 1024 * 1024 * 100    // 100MB total
)
public class FileUploadServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
        "video/mp4", "video/mpeg", "video/quicktime", "video/webm"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain"
    );

    @Override
    public void init() throws ServletException {
        super.init();

        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            System.out.println("Directorio de uploads creado: " + uploadPath);
        }

        createSubdirectory(uploadPath, "images");
        createSubdirectory(uploadPath, "videos");
        createSubdirectory(uploadPath, "documents");
        createSubdirectory(uploadPath, "others");
    }

    private void createSubdirectory(String basePath, String subDir) {
        File dir = new File(basePath + File.separator + subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            List<String> uploadedFiles = new ArrayList<>();

            for (Part part : req.getParts()) {
                if (part.getName().equals("file") && part.getSize() > 0) {
                    String fileName = extractFileName(part);
                    String contentType = part.getContentType();

                    if (!isValidFileType(contentType)) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("{\"error\":\"Tipo de archivo no permitido: " + contentType + "\"}");
                        return;
                    }

                    String subDir = determineSubdirectory(contentType);

                    String uniqueFileName = generateUniqueFileName(fileName);

                    String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
                    String fullPath = uploadPath + File.separator + subDir + File.separator + uniqueFileName;

                    saveFile(part, fullPath);

                    String fileUrl = req.getContextPath() + "/" + UPLOAD_DIR + "/" + subDir + "/" + uniqueFileName;
                    uploadedFiles.add(fileUrl);

                    System.out.println("Archivo subido: " + uniqueFileName + " (" + contentType + ")");
                }
            }

            if (uploadedFiles.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"No se recibieron archivos\"}");
                return;
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("count", uploadedFiles.size());

            StringBuilder filesJson = new StringBuilder("[");
            for (int i = 0; i < uploadedFiles.size(); i++) {
                filesJson.append("\"").append(uploadedFiles.get(i)).append("\"");
                if (i < uploadedFiles.size() - 1) {
                    filesJson.append(",");
                }
            }
            filesJson.append("]");

            response.addProperty("files", filesJson.toString());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error al subir archivo: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject info = new JsonObject();
        info.addProperty("service", "File Upload Service");
        info.addProperty("endpoint", "/api/upload");
        info.addProperty("method", "POST");
        info.addProperty("maxFileSize", "50MB");
        info.addProperty("maxRequestSize", "100MB");
        info.addProperty("allowedTypes", "images (jpg, png, gif, webp), videos (mp4, webm), documents (pdf, doc, xls, txt)");

        resp.getWriter().write(info.toString());
    }

    private String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] items = contentDisposition.split(";");

        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }

        return "file_" + System.currentTimeMillis();
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf(".");

        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }

        return new ObjectId().toString() + extension;
    }

    private boolean isValidFileType(String contentType) {
        if (contentType == null) return false;

        return ALLOWED_IMAGE_TYPES.contains(contentType) ||
               ALLOWED_VIDEO_TYPES.contains(contentType) ||
               ALLOWED_DOCUMENT_TYPES.contains(contentType);
    }

    private String determineSubdirectory(String contentType) {
        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return "images";
        } else if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
            return "videos";
        } else if (ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            return "documents";
        } else {
            return "others";
        }
    }

    private void saveFile(Part part, String filePath) throws IOException {
        try (InputStream input = part.getInputStream()) {
            Path path = Paths.get(filePath);
            Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
