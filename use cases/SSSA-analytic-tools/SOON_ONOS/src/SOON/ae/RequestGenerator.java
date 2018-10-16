
package ae;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import Scheduler.Admin;


public class RequestGenerator {

    public static Random universalRand;
    static double[] temps;
    static double load = 100;//remains fixed 
    static long seed = 1;
    //static int NumberRequests = 3;
    public static double[] serviceTime;
    static double service_time = 150;

    static long MODULE = 2147483647;
    static long A = 16807;
    static long LASTXN = 127773;
    static long UPTOMOD = -2836;
    static double RATIO = 0.46566128e-9;
    static double ALPHA = 1.9;

    public static void GenerateRequests(int NumberRequests) throws IOException {
        
        temps = new double[NumberRequests + 1];
        serviceTime = new double[NumberRequests + 1];

        System.out.println("Arrival times");

        File fTa = new File("arrivalTimes.txt");
        
        FileWriter fwTa = new FileWriter(fTa, false);
        BufferedWriter bwTa = new BufferedWriter(fwTa);
        //Variables.temps[0] = 0;
        temps[0] = 0;
        double intArrivTime;
        for (int k = 1; k <= NumberRequests; k++) {
            temps[k] = temps[k - 1] + 4000;//negexp(load, seed);
            //temps[k] = temps[k - 1] + 25;//for debugging 
            seed = rnd32(seed);
            System.out.println(k + " " + temps[k]);
            intArrivTime = temps[k] - temps[k - 1];
            bwTa.append(Integer.toString(k)).append(" ");
            bwTa.append(Double.toString(intArrivTime));
            bwTa.newLine();
            bwTa.flush();
        }
        bwTa.close();

        System.out.println("Service times");

        //Variables. = 0;
        for (int k = 0; k <= NumberRequests; k++) {

            serviceTime[k] = 5000;//negexp(service_time, seed);
            //serviceTime[k] = 200; //for debugging
            seed = rnd32(seed);

            System.out.println(k + " " + serviceTime[k]);
        }
        
        
        for (int i = 1; i<= NumberRequests; i++)
         {

         Admin n1 = new Admin(i, new Request(), 0);
         n1.setAdmin(n1);

         }
    }

    /**
     * ******************************************************************
     */
    public static long rnd32(long seed) {
        long times, rest, prod1, prod2;
        times = seed / LASTXN;
        rest = seed - times * LASTXN;
        prod1 = times * UPTOMOD;
        prod2 = rest * A;
        seed = prod1 + prod2;
        if (seed < 0) {
            seed = seed + MODULE;
        }
        return (seed);
    }

    public static double negexp(double mean, long seed) {
        double u;

//seed=rnd32(seed);
        u = seed * RATIO;

        double var = (-mean * Math.log(u));
        return var;
    }

    public static double[] getServiceTime() {
        return serviceTime;
    }

    public static double[] getArrivalTime() {
        return temps;
    }

    /**
     * *********************************************************************
     */
}
