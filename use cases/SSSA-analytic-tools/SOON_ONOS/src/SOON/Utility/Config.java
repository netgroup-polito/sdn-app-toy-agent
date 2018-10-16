package Utility;

import java.io.*;
import java.util.*;

public class Config
{
	
	protected Config ()
	{
		try
		{
			 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("config.txt"))));
												
				String line = br.readLine();
				String key = "";
				String value = "";
				while(line!=null) 
				{
					StringTokenizer st = new StringTokenizer(line, "=");
					if(st.hasMoreTokens())
						key = st.nextToken();
					if(st.hasMoreTokens())
					{
                                          
						value = st.nextToken();  
                                               // System.out.println(key.toString()+","+value);
						map.put(key,value);
					}
		      line = br.readLine();
				}
		    br.close();
		  }
      catch (Exception e) 
      {
    		e.printStackTrace();      
    	}	
		}
	
	private static Config instance = null;
	
	static public Config getInstance ()
	{
		if(instance == null)
			instance = new Config();
		return instance;
	}

	private HashMap map = new HashMap();
	

	public String getString (String key)
	{
		return ((String) map.get(key));
	}	
	
	public int getInt(String key)
	{
		return Integer.parseInt((String) map.get(key));
	}	
}