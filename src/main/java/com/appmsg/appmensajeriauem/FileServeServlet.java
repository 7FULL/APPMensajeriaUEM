package com.appmsg.appmensajeriauem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(name = "fileServeServlet", value = "/uploads/*")
public class FileServeServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String requestedFile = req.getPathInfo();

        if (requestedFile == null || requestedFile.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("No file specified");
            return;
        }

        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        Path filePath = Paths.get(uploadPath + requestedFile).normalize();

        File file = filePath.toFile();
        String canonicalUploadPath = new File(uploadPath).getCanonicalPath();
        String canonicalFilePath = file.getCanonicalPath();

        if (!canonicalFilePath.startsWith(canonicalUploadPath)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("Access denied");
            return;
        }

        if (!file.exists() || !file.isFile()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("File not found");
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());
        resp.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

        // Thgs is just for performance via cache
        resp.setHeader("Cache-Control", "public, max-age=31536000"); // 1year

        Files.copy(filePath, resp.getOutputStream());
        resp.getOutputStream().flush();
    }
}
