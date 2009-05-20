/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rvision;
import java.io.IOException;
import javax.jmdns.*;

/**
 *
 * @version $Id$
 * @author $Author$
 */
public class JmdnsTesting {
    public static void main(String[] args) throws IOException, InterruptedException {

        JmDNS jmdns = JmDNS.create();
        System.out.println("Host: " + jmdns.getHostName() );
        System.out.println("Interface: " + jmdns.getInterface() );
        String type = "_rvision._udp.local.";

        ServiceInfo sis[] = jmdns.list(type);
        for( ServiceInfo si : sis ){
            System.out.println("Service : " + si.getServer() + "--"
                + si.getPort() + "--" + si.getNiceTextString() );
        }

        jmdns.addServiceTypeListener( new ServiceTypeListener() {
            public void serviceTypeAdded(ServiceEvent evt) {
                System.out.println("serviceTypeAdded: " + evt);
            }
        } );

        jmdns.addServiceListener(type, new ServiceListener() {

            public void serviceAdded(ServiceEvent evt) {
                System.out.println("serviceAdded: " + evt);
            }

            public void serviceRemoved(ServiceEvent evt) {
                System.out.println("serviceRemoved: " + evt);
            }

            public void serviceResolved(ServiceEvent evt) {
                System.out.println("serviceResolved: " + evt);
            }
        });


        //ServiceInfo si = ServiceInfo.create(type, "rvision", 4000, "RVision Camera Controller" );
        //jmdns.registerService( si );

        //UdpServer us = new UdpServer(1234);
        //us.start();
        //Thread.sleep(1000);
        //us.setPort(1235);

    }
}
