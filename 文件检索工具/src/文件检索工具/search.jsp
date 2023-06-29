<%@ page import="java.io.File" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>搜索结果</title>
    <link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>
<h1>搜索结果</h1>
<div id="content">
    <p>搜索路径: <%= ((File) request.getAttribute("file")).getAbsolutePath() %></p>
    <p>搜索词: <%= request.getAttribute("searchTerm") %></p>
    <ul>
        <%
            List<String> searchResults = (List<String>) request.getAttribute("searchResults");
            request.setAttribute("searchResults", searchResults);
            if (searchResults != null && !searchResults.isEmpty()) {
                for (String result : searchResults) {
        %>
        <li><%= result %></li>
        <%
            }
        } else {
        %>
        <li>未找到匹配的结果。</li>
        <%
            }
        %>
    </ul>
    <a href="downloadResults.jsp?searchResults=<%= URLEncoder.encode(searchResults.toString(), "UTF-8") %>" class="btn btn-primary">下载结果</a>


</div>
<script src="jquery.min.js"></script>
<script src="script.js"></script>

</body>
</html>
