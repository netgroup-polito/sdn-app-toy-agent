
package Scheduler;

import java.util.Random;
import Scheduler.*;

public class RequestGenerator {
    
public static Random universalRand;

static long MODULE = 2147483647;
static long A = 16807;
static long LASTXN = 127773;
static long UPTOMOD = -2836;
static double RATIO = 0.46566128e-9; 
static double ALPHA = 1.9;
    
    /*
 public static void GenerateRequests()
 {
     
Variables.temps[0] = 0;
for (int k=1; k<=Variables.NumberRequests; k++)
{
Variables.temps[k] = Variables.temps[k-1] + negexp(Variables.load,Variables.seed);
Variables.seed = rnd32(Variables.seed);
}

         for (int i = 1; i<=Variables.NumberRequests; i++)
       {

        Admin n1 = new Admin(i, new Request(), Variables.temps[i]);
        n1.setAdmin(n1);

       }

 }
 
 /*********************************************************************/

public static long rnd32(long seed) 
{
long times, rest, prod1, prod2;
times = seed / LASTXN;
rest  = seed - times * LASTXN;
prod1 = times * UPTOMOD;
prod2 = rest * A;
seed  = prod1 + prod2;
if (seed < 0) seed = seed + MODULE;
return (seed);
}


 public static double negexp(double mean, long seed) 
{
double u;

seed=rnd32(seed);

u=seed*RATIO;

double var = (-mean*Math.log(u));
return var;
}
    
    
 /************************************************************************/   
    
}
