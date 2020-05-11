package com.ledig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    public static Server createServer(int port, Resource baseResource)
    {
        Server server = new Server(port);
        /*
        ResourceHandler resourceHandler = new ResourceHandler();

        // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
        // In this example it is the current directory but it can be configured to anything that the jvm has access to.
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setBaseResource(baseResource);

        // Add the ResourceHandler to the server.
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
        server.setHandler(handlers);
        //server.setHandler(new MyHandler());
        */

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(FormServlet.class, "/*");
        handler.addServletWithMapping(SendServlet.class, "/sendtext");

        return server;
    }

    public static void main( String[] args ) throws Exception
    {
        int port = 8080;
        Path userDir = Paths.get("src/main/java/com/ledig");
        PathResource pathResource = new PathResource(userDir);
        Server server = createServer(port, pathResource);
        server.start();
        server.join();
    }

    public static class FormServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws IOException
        {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println("<form action=\"/sendtext\">\n" +
                    "    <label for=\"phone\">Phone number:</label><br />\n" +
                    "    <input type=\"text\" id=\"phone\" name=\"phone\" placeholder=\"XXX-XXX-XXXX\"><br />\n" +
                    "    <label for=\"msg\">Text message:</label><br />\n" +
                    "    <input type=\"text\" id=\"msg\" name=\"msg\"> <br /><br />\n" +
                    "    <input type=\"submit\" value=\"Submit\">\n" +
                    "</form>");
        }
    }

    public static class SendServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws IOException, ServletException
        {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");

            System.out.println("Query string: " + request.getQueryString());
            String[] queryParts = request.getQueryString().split("&");
            Map<String, String> queryVals = new HashMap<>();
            for (String s : queryParts) {
                String[] splitParts = s.split("=");
                queryVals.put(splitParts[0], splitParts[1]);
            }

            System.out.println("Number: " + queryVals.get("phone"));
            System.out.println("Text: " + queryVals.get("msg"));

        }
    }
}
