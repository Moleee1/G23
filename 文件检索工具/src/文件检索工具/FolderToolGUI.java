package com.test.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/folderServlet")
public class FolderServlet extends HttpServlet {

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String encodedPath = request.getParameter("path");
        String decodedPath = java.net.URLDecoder.decode(encodedPath, "UTF-8");
        File folder = new File(decodedPath);
        File[] files = folder.listFiles();
        request.setAttribute("files", files);
        request.getRequestDispatcher("list.jsp").forward(request, response);
    }

}
