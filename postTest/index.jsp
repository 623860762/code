<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>My JSP 'index.jsp' starting page</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
  </head>
  
  <body>
    This is my JSP page. <br>
    <% 
    Long a = System.currentTimeMillis();
    out.println("第一次开始输出....");	
    Long b = System.currentTimeMillis();
    while(b-a<60000){
    	b = System.currentTimeMillis();
    }
    out.println(b-a);
    out.println("60s之后第一次开始输出....");
    
    a = System.currentTimeMillis();
    out.println("第二次开始输出....");	
    b = System.currentTimeMillis();
    while(b-a<60000){
    	b = System.currentTimeMillis();
    }
    out.println(b-a);
    out.println("60s之后第二次开始输出....");	
    %>
  </body>
</html>
