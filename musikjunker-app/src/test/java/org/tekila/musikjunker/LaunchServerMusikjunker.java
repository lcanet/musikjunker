package org.tekila.musikjunker;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Embedded launcher
 * @author lc
 *
 */
public class LaunchServerMusikjunker {

	public static void main(String[] args) throws Exception {

		Server server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        server.addConnector(connector);
        
        WebAppContext wac = new WebAppContext();
        wac.setContextPath("/");
        wac.setWar("./src/main/webapp");
        wac.setDefaultsDescriptor("jetty-webdefault.xml");
        server.setHandler(wac);

        server.setStopAtShutdown(true);
        server.start();
        
		
	}
}
