package org.onebusaway.king_county_metro.mybus_siri;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class DummySiriMain {
  public static void main(String[] args) throws Exception {
    int port = 8080;
    if (args.length == 1)
      port = Integer.parseInt(args[0]);

    Server server = new Server(port);
    Context context = new Context(server, "/", Context.SESSIONS);

    HttpServlet servlet = new MyServlet();
    context.addServlet(new ServletHolder(servlet), "/*");

    server.start();
  }

  private static class MyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

      System.out.println("here we go");
    }
  }
}
