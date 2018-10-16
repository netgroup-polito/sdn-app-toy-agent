
package DSE;


public class Hex2Decimal {
    
    
    public static Long hexDec(String num)
{
    Long value = 0L;
    
    num = num.substring(18);
    switch (num)
            {
                case "a":
                    value = 10L;
                    break;
                case "b":
                    value = 11L;
                    break;
                case "c":
                    value = 12L;
                    break;
                case "d":
                    value = 13L;
                    break;
                case "e":
                    value = 14L;
                    break;
                case "f":
                    value = 15L;
                    break;
                default:
                    value = Long.parseLong(num);
            }

     return value;

}
    
   public static String DecHex(long num)
{
    String value = "";
    
    int intnum = (int)num;
    switch (intnum)
            {
                case 10:
                    value = "a";
                    break;
                case 11:
                    value = "b";
                    break;
                case 12:
                    value = "c";
                    break;
                case 13:
                    value = "d";
                    break;
                case 14:
                    value = "e";
                    break;
                case 15:
                    value = "f";
                    break;
                default:
                    value = String.valueOf(intnum);
            }

     return value;

}
    
}
