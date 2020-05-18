package com.ledig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Hello world!
 *
 */
public class App 
{
    static Regions clientRegion = Regions.US_EAST_1;
    static String bucketName = "wledig-test-project-bucket";
    static AmazonS3 s3Client;

    static final String ACCOUNT_SID = "AC4b0d28e299e1e1001568785f88aada49";
    static final String AUTH_TOKEN = "auth_token_here";

    public static Server createServer(int port, Resource baseResource)
    {
        Server server = new Server(port);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(FormServlet.class, "/*");
        handler.addServletWithMapping(SendServlet.class, "/sendtext");


        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .build();

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
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
                    "    <input type=\"text\" id=\"phone\" name=\"phone\" placeholder=\"XXXXXXXXXX\"><br />\n" +
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

            String phoneNum = queryVals.get("phone");
            String textMsg = queryVals.get("msg");
            System.out.println("Number: " + phoneNum);
            System.out.println("Text: " + textMsg);

            // Do S3 logging
            s3Client.putObject(bucketName, "log (" + new Date() + ")",
                    "number: " + phoneNum + "\ntext: " + textMsg);

            // Send message with Twilio
            Message message = Message
                    .creator(new PhoneNumber("+1" + phoneNum), // to
                            new PhoneNumber("+12058912621"), // from
                            textMsg)
                    .create();

            System.out.println("Message sent: " + message.getSid());
        }
    }
}
