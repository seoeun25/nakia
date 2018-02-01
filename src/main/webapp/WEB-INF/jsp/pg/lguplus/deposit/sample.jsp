<%@ page contentType="text/html; charset=UTF-8" %>

<%
    String targetUrl =
            "/page/v1/lguplus/deposit/reservation" +
                    "?amount=19900&platform=web&point=0&store=web&paymentType=deposit&locale=ko-KR&" +
                    "_lz_userId=6066665726148608&isMobile=false&" +
                    "_lz=3345d250-5165-439c-a8de-ce08c3c7e5ab&currency=KRW&productId=6395783134314496";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>LG유플러스 전자결제 샘플 페이지 (XPay)</title>
    <script type="text/javascript">


        function showPopup() {
            window.open("<%=targetUrl%>", "a", "width=1000, height=700, left=300, top=300");
        }

    </script>
</head>
<body>
<input type="button" value="팝업창 호출" onclick="showPopup();" />
</body>
</html>
