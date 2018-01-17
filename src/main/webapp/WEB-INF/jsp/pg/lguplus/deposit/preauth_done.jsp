<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>

<%
    request.setCharacterEncoding("utf-8");

    String LGD_RESPCODE = request.getParameter("LGD_RESPCODE");
    String LGD_RESPMSG = request.getParameter("LGD_RESPMSG");

    Map payReqMap = request.getParameterMap();
    System.out.println("-- page. LGD_RESPMSG = " + LGD_RESPMSG);
    boolean isMobile = request.getAttribute("isMobile")  == null ? false :
            Boolean.valueOf(request.getAttribute("isMobile").toString());
    System.out.println("-- page. isMobile = " + isMobile);

%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>LG U+ 전자결제 서비스</title>
    <link href="https://xpay.lgdacom.net:7443/xpay/css/red_v25/import.css" rel="stylesheet" type="text/css">
    <!--[if IE 6]>
    <link rel="stylesheet" href='https://xpay.lgdacom.net:7443/xpay/css/red_v25/ie6.css' type="text/css"
          charset="euc-kr" media="all"/>
    <![endif]-->
    <style>
        html, body { height: 100%; margin: 0; }
        body { width: auto; height: 100%; font-family: 'Malgun Gothic', Helvetica, sans-serif; }
        h1 { margin: 0; display: none; }
        .header { width: auto; height: 34px; position: absolute; top: 0; left: 0; right: 0; }
        .header h2 { width: 100%; text-indent: 30px; overflow: hidden; line-height: 36px; }
        .section { width: auto; height: auto; padding: 0; position: absolute; top: 34px; left: 0; right: 0; bottom: 24px; }
        .container { position: absolute; top: 0; left: 0; right: 0; bottom: 0; z-index: 2; }
        .msg { position: absolute; top: 50%; left: 0; right: 0; font-size: 14px; color: #404040; text-align: center; }
        .is-preloader{ position:absolute; top: 0; left: 0; right: 0; bottom: 0; z-index: 1; }.is-preloader:before{content:'';position:absolute;top:50%;left:50%;width:28px;height:28px;margin:-14px 0 0 -14px;background:url(https://cdn.lezhin.com/assets/svg/loader-red.svg) no-repeat 50% 50%;will-change:transform;-webkit-animation:rotating 1s linear infinite;-moz-animation:rotating 1s linear infinite;-ms-animation:rotating 1s linear infinite;animation:rotating 1s linear infinite;z-index:100}.is-preloader--spread:before{width:26px;height:26px;border:2px solid #e50f24;border-radius:50%;background:none;display:block;will-change:opacity, transform;-webkit-animation:spread 1s ease-out infinite;-moz-animation:spread 1s ease-out infinite;-ms-animation:spread 1s ease-out infinite;animation:spread 1s ease-out infinite}.is-preloader--big:before{width:46px;height:46px;margin:-80px 0 0 -23px}.is-preloader--big.is-preloader--spread:before{width:42px;height:42px;border:4px solid #e50f24}.is-preloader--white:before{background-image:url(https://cdn.lezhin.com/assets/svg/loader-white.svg)}.is-preloader--white.is-preloader--spread:before{background-image:none;border-color:#fff}.is-preloader{color:transparent !important}@-webkit-keyframes rotating{from{-webkit-transform:rotate(0deg);-ms-transform:rotate(0deg);transform:rotate(0deg)}to{-webkit-transform:rotate(360deg);-ms-transform:rotate(360deg);transform:rotate(360deg)}}@keyframes rotating{from{-webkit-transform:rotate(0deg);-ms-transform:rotate(0deg);transform:rotate(0deg)}to{-webkit-transform:rotate(360deg);-ms-transform:rotate(360deg);transform:rotate(360deg)}}@-webkit-keyframes fade{0%,100%{opacity:0}50%{opacity:1}}@keyframes fade{0%,100%{opacity:0}50%{opacity:1}}@-webkit-keyframes spread{0%{transform:scale(0);opacity:0}50%{opacity:1}100%{transform:scale(1);opacity:0}}@keyframes spread{0%{transform:scale(0);opacity:0}50%{opacity:1}100%{transform:scale(1);opacity:0}}
        .mobile .is-preloader { display: none; }
        .mobile .msg { margin-top: 50px; }
        #LGD_CANCEL { position: absolute; right: 16px; top: 10px; max-width: 21px;}
        #main_agreed_info { width: auto; height: auto; padding: 0; position: absolute; top: 0; left: 0; right: 0; bottom: 0; float: none; }
        .footer { width: 100%; height: 24px; position: absolute; bottom: 0; left: 0; right: 0; }
    </style>
    <script type="text/javascript">
        function setLGDResult() {
            var mobile = <%=isMobile%>;
            if (mobile) {
                var lgdReturnInfo = document.getElementById("LGD_RETURNINFO");
                document.getElementById("body_id").className ='mobile';
                lgdReturnInfo.target = "_self";
                lgdReturnInfo.action = "/page/v1/lguplus/deposit/authentication";
                lgdReturnInfo.submit();
            } else {
                window.parent.postMessage({ message: 'payment_return' }, window.origin);
            }
        }
    </script>
</head>

<body id="body_id" bgcolor="white" onload="setLGDResult()">

<div class="header">
    <h1>
        <img src="https://xpay.lgdacom.net:7443/xpay/image/red_v25/common/logo.jpg" alt="엘지유플러스 전자결제">
    </h1>
    <h2>가상계좌(무통장입금)</h2>
</div>

<div class="section">
    <div class="contWrap" id="main_agreed_info">
        <form method="post" name="LGD_RETURNINFO" id="LGD_RETURNINFO">
            <%
                for (Iterator i = payReqMap.keySet().iterator(); i.hasNext(); ) {
                    Object key = i.next();
                    if (payReqMap.get(key) instanceof String[]) {
                        String[] valueArr = (String[]) payReqMap.get(key);
                        for (int k = 0; k < valueArr.length; k++)
                            out.println("<input type='hidden' name='" + key + "' id='" + key + "'value='" + valueArr[k] + "'/>");
                    } else {
                        String value = payReqMap.get(key) == null ? "" : (String) payReqMap.get(key);
                        out.println("<input type='hidden' name='" + key + "' id='" + key + "'value='" + value + "'/>");
                    }
                }
            %>
        </form>
        <div class="is-preloader is-preloader--big"></div>
        <div class="container">
            <h1> 인증결과 </h1>
            <p class="msg">결재가 진행 중입니다. 잠시 기다려 주세요.</p>
        </div>
    </div>
</div>

<div class="footer">
    <address>
        고객센터 1544-7772 / <a href="mailto:ecredithelp@lguplus.co.kr">ecredithelp@lguplus.co.kr</a>
    </address>
</div>

</body>
</html>
