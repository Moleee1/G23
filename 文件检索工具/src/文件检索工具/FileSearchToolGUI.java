package com.test.servlet;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/searchServlet")
public class SearchServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String encodedPath = request.getParameter("path");
        String decodedPath = java.net.URLDecoder.decode(encodedPath, "UTF-8");
        File file = new File(decodedPath);
        String searchTerm = request.getParameter("searchTerm");

        if (file.exists() && file.isFile() && (file.getName().endsWith(".pdf") || file.getName().endsWith(".doc") || file.getName().endsWith(".docx"))) {
            List<String> results = searchInFile(decodedPath, searchTerm);
            request.setAttribute("searchResults", results);
        }

        request.setAttribute("file", file);
        request.setAttribute("searchTerm", searchTerm);
        request.getRequestDispatcher("search.jsp").forward(request, response);
    }

    /**
     * 读取文件内容
     * @param file
     * @param searchTerm
     * @return
     * @throws IOException
     */
    private List<String> searchInFile(String file, String searchTerm) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        XWPFDocument document = new XWPFDocument(fis);
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        List<String> results = new ArrayList<>();
        String[] paragraphs = extractor.getText().split("\n");
        int lineNumber = 1;
        for (String paragraph : paragraphs) {
            System.out.println(paragraph);
            if (paragraph.contains(searchTerm)) {
                results.add("行号: " + lineNumber + ", 内容: " + paragraph);
            }
            lineNumber++;
        }

        fis.close();
        return results;
    }
}
