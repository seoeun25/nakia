<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.lezhin.panther.util.Util" %>

<%
request.setCharacterEncoding("utf-8");

String LGD_RESPCODE = request.getParameter("LGD_RESPCODE");
String LGD_RESPMSG 	= request.getParameter("LGD_RESPMSG");

LGD_RESPMSG = Util.convertEncoding(LGD_RESPMSG, "ISO-8859-1", "EUC-KR");
Map payReqMap = request.getParameterMap();
Map newPayReqMap = new HashMap(payReqMap);
newPayReqMap.put("LGD_RESPMSG", LGD_RESPMSG);
%>
<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<head>
	<script type="text/javascript">

		function setLGDResult() {
			try {
                parent.payment_return();
			} catch (e) {
				alert(e.message);
			}
		}
		
	</script>
</head>
<body onload="setLGDResult()">
<p><h1>RETURN_URL (인증결과)</h1></p>
<div>
<p>LGD_RESPCODE (결과코드) : <%= LGD_RESPCODE %></p>
<p>LGD_RESPMSG (결과메시지): <%= LGD_RESPMSG %></p>
	<form method="post" name="LGD_RETURNINFO" id="LGD_RETURNINFO">
	<%
	for (Iterator i = newPayReqMap.keySet().iterator(); i.hasNext();) {
		Object key = i.next();
		if (newPayReqMap.get(key) instanceof String[]) {
			String[] valueArr = (String[])newPayReqMap.get(key);
			for(int k = 0; k < valueArr.length; k++)
				out.println("<input type='hidden' name='" + key + "' id='"+key+"'value='" + valueArr[k] + "'/>");
		} else {
			String value = newPayReqMap.get(key) == null ? "" : (String) newPayReqMap.get(key);
			out.println("<input type='hidden' name='" + key + "' id='"+key+"'value='" + value + "'/>");
		}
	}
	%>
	</form>
</div>
</body>
</html>