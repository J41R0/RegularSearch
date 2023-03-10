import java.util.ArrayList;

import algorithms.RegularSearch.RegularSearch;
import algorithms.YYC.YYC;
import py4j.GatewayServer;

public class App {

    public static String testTTAlgs(ArrayList<ArrayList<Integer>> myMB) {
        // System.out.println("MB rows:");
        // System.out.println(myMB.size());
        String result = "";
        String output = "{";
        result = YYC.FindTT(myMB);
        output += "'YYC':" + result + ",";
        result = RegularSearch.FindTT(myMB);
        output += "'RegularSearch':" + result;
        output += '}';
        return output;
    }

    public static void main(String[] args) throws Exception {
        GatewayServer gatewayServer = new GatewayServer(new App());
        gatewayServer.start();
        System.out.println("Running gateway sever!!!");
    }
}
