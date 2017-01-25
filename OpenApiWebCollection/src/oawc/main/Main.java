package oawc.main;

import oawc.getTemp.RssReadTemp;


public class Main {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[][] arr = new RssReadTemp().getTemp();
		for(String[] ar : arr)
			for(String s : ar)
				System.out.println(s);
	}
}
