package com.shelf.command;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

public class CommandServlet extends HttpServlet {
    
    private static final String CODING = "UTF-8";
    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger();
    
    private ServletConfig config;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{

            String command = request.getParameter("run");
            if( command == null ) command = "";

            HashMap data = new HashMap();
            data.put("command", command);
            data.put("ip", request.getRemoteAddr());
            data.put("datetime", String.valueOf(LocalDateTime.now()) );
            data = command.equals("system") ? getSystem( data ) : 
                   command.equals("env")    ? getEnvironment( data ) :
                   command.equals("server") ? getServer( data ) :
                   command.equals("token")  ? getToken( data ) :
                                              getError( data, "UnknownCommand" );

            byte[] bytes = JSONObject.toJSONString( data ).getBytes(CODING);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType( "application/json; charset="+CODING );
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate" );
            response.setContentLength( bytes.length );
            try( ServletOutputStream stream = response.getOutputStream() ){
                stream.write( bytes );
                stream.flush();
            }
            
            LOG.info("response={}", data);
        } catch(Exception e ){ 
            LOG.error(e);
        }
    }
    
    private HashMap getServer( HashMap map ){
        HashMap result = new HashMap();
        if( config != null ){
            ServletContext context = config.getServletContext();
            result.put("server-info", context.getServerInfo() );
            result.put("virtual-server-name", context.getVirtualServerName() );
            result.put("context-path", context.getContextPath() );
            result.put("session-timeout", context.getSessionTimeout() );
            result.put("servlet-name", config.getServletName() );
            result.put("servlet-context-name", context.getServletContextName() );
            result.put("servlet-info", getServletInfo() );
        }
        map.put("result", result );
        return map;
    }
    
    private HashMap getSystem( HashMap map ){
        map.put("result", new HashMap(System.getProperties()) );
        return map;
    }
    
    private HashMap getEnvironment( HashMap map ){
        map.put("result", new HashMap(System.getenv()) );
        return map;
    }
    
    private HashMap getToken( HashMap map ){
        HashMap result = new HashMap();
        result.put("token", UUID.randomUUID().toString() );
        map.put("result", new HashMap(result) );
        return map;
    }
    
    private HashMap getError( HashMap map, String error ){
        map.put("error", error );
        return map;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    public void init( ServletConfig config ) throws ServletException {
        this.config = config;
        LOG.info("Servlet init");
        super.init( config );
    }
    
    @Override
    public void destroy(){
        LOG.info("Servlet destroy");
        super.destroy();
    }
    
    @Override
    public String getServletInfo() {
        return "CommandServletInfo";
    }

}
