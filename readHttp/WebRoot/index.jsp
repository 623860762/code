<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
    <% 
    Long a = System.currentTimeMillis();
    out.println("{'name':'张宁'}");	
    Long b = System.currentTimeMillis();
    while(b-a<4000){
    	b = System.currentTimeMillis();
    }

    
    a = System.currentTimeMillis();
    b = System.currentTimeMillis();
    while(b-a<4000){
    	b = System.currentTimeMillis();
    }
    out.println("{'aaa':'111'}");
    
    out.println("{'bb':11");
    out.println("}");
    
    out.println("{'cc':'33',");
    a = System.currentTimeMillis();
    b = System.currentTimeMillis();
    while(b-a<4000){
    	b = System.currentTimeMillis();
    }
    out.println("'ee':'22',");
    out.println("'ff':'66'}");
    %>
