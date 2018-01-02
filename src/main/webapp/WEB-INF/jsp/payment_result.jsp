<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%

    String userId = request.getParameter("__u");
    String isMobile = request.getParameter("isMobile");
    String isApp = request.getParameter("isApp");
    String returnTo = "";
    String reason = "";
    String bank = "";
    String accountNumber = "";
    String date = "";

    if (request.getParameter("returnTo") != null) {
        returnTo = request.getParameter("returnTo").toString();
    }
    if (request.getParameter("reason") != null) {
        reason = request.getParameter("reason").toString();
    }
    if (request.getParameter("bank") != null) {
        bank = request.getParameter("bank").toString();
    }
    if (request.getParameter("accountNumber") != null) {
        accountNumber = request.getParameter("accountNumber").toString();
    }
    if (request.getParameter("date") != null) {
        date = request.getParameter("date").toString();
    }

%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Hello Panther</title>
</head>
<body>
<h3> userId : <%= userId%>
</h3>
<h3> isMobile : <%= isMobile%>
</h3>
<h3> isApp : <%=isApp%>
</h3>
<h3> returnTo : <%=returnTo%>
</h3>
<h3> reason : <%=reason%>
</h3>
<h3> bank : <%=bank%>
</h3>
<h3> accountNumber : <%=accountNumber%>
</h3>
<h3> date : <%=date%>
</h3>

<div>Hello, azrael</div>
</body>
</html>