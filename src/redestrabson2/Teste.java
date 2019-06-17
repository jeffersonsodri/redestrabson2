package redestrabson2;

import java.util.HashMap;

public class Teste {
	static String s = "1aaaaaaaaaaa2";
	static String total = "";
	public static void main (String [] args) {
		byte[] shaz = new byte[512];
		HashMap<Integer,byte[]>  test = new HashMap<Integer,byte[]>();
		shaz = s.getBytes();
		test.put(0,shaz);
		test.put(1,shaz);
		test.put(2,shaz);
		test.put(3,shaz);
		test.put(4,shaz);
		test.put(5,shaz);
		total = "eeeeeeeeeee";
		for(int b=0;b<test.size();b++) {
			String s = new String(test.get(b));
			total = total + s;
		}	
		System.out.println(total);
	}
}
