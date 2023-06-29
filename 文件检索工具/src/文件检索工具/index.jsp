<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <title>文件夹页面</title>
  <link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>
<h1>文件夹页面</h1>
<div id="content">
  <ul>
    <li><a href="folderServlet?path=<%=java.net.URLEncoder.encode("C:", "UTF-8")%>">C:</a></li>
    <li><a href="folderServlet?path=<%=java.net.URLEncoder.encode("D:", "UTF-8")%>">D:</a></li>
  </ul>

</div>
<script src="jquery.min.js"></script>
<script src="script.js"></script>
</body>
</html>
