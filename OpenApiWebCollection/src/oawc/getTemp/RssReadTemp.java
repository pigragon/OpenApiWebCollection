package oawc.getTemp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class RssReadTemp {
	
	public String[][] getTemp(){
		ArrayList<TempVo> arraylist = null;
		
		try {	
			 arraylist = getTempo();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[][] ret = new String[arraylist.size()][7];
		
		int i=0;
		for(TempVo vo : arraylist){
			ret[i][0] = vo.getDate();
			ret[i][1] = vo.getTime();
			ret[i][2] = vo.getTemp();
			ret[i][3] = vo.getDONAM();
			ret[i][4] = vo.getDOCOD();
			ret[i][5] = vo.getGUNNAM();
			ret[i][6] = vo.getGUNCOD();
			i++;
		}
		return ret;
	}
	


	public ArrayList<TempVo> getTempo() throws Exception{
		ArrayList<TempVo> arraylist = new ArrayList<TempVo>();
		
		URL sido = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt");
		InputStreamReader isSIDO = new InputStreamReader(sido.openConnection().getInputStream(), "UTF-8");

		JSONArray sidoCod = (JSONArray)JSONValue.parseWithException(isSIDO);
		for(int i=0; i<sidoCod.size(); i++){
			JSONObject objSIDO = (JSONObject)sidoCod.get(i);
			
			URL gungu = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl."+objSIDO.get("code")+".json.txt");
			InputStreamReader isGUNGU = new InputStreamReader(gungu.openConnection().getInputStream(), "UTF-8");
			JSONArray gunguCod = (JSONArray)JSONValue.parseWithException(isGUNGU);
			for(int j=0; j<gunguCod.size(); j++){
				JSONObject objGUNGU = (JSONObject)gunguCod.get(j);
				
				

				URL tempURL = new URL("http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone="+objGUNGU.get("code"));
				URLConnection tempconnection = tempURL.openConnection();
				Document doc = parseXML(tempconnection.getInputStream());
				NodeList header = doc.getElementsByTagName("header");
				String DATE = null;
				String TIME = null;
				String DAY = null;
				
				for (Node node = header.item(0).getFirstChild(); node != null; node = node.getNextSibling()) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						if (node.getNodeName().equals("tm")) {
							DATE = node.getTextContent().substring(0,8);
						}
					}
				}
				NodeList data = doc.getElementsByTagName("data");
				for(int k=0; k<data.getLength(); k++){
					for (Node node = data.item(k).getFirstChild(); node != null; node = node.getNextSibling()) {
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							
							if (node.getNodeName().equals("hour")) {
								
								TIME = node.getTextContent()+"0000";
							}
							else if(node.getNodeName().equals("day")){
								DAY = node.getTextContent();
							}
							else if (node.getNodeName().equals("temp")) {
								TempVo vo = new TempVo();
								vo.setDate(setDate(DATE, DAY));
								if(TIME.equals("240000")){
									vo.setDate(setDate(vo.getDate(), "1"));
									TIME = "000000";
								}
								vo.setTime(TIME);
								vo.setTemp(node.getTextContent());
								vo.setDONAM((String) objSIDO.get("value"));
								vo.setDOCOD((String) objSIDO.get("code"));
								vo.setGUNNAM((String) objGUNGU.get("value"));
								vo.setGUNCOD((String) objGUNGU.get("code"));
								arraylist.add(vo);
							}
						}
					}
				}
			}
		}
		return arraylist;
	}


	public String setDate(String date, String oper){
		SimpleDateFormat df = new SimpleDateFormat("yyyymmdd");
		Date dat = null;
		
		try {
			dat = df.parse(date);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dat);
		cal.add(Calendar.DATE, Integer.parseInt(oper));
		
		date = df.format(cal.getTime());
		return date;
		
	}
	
	public Document parseXML(InputStream stream) throws Exception {
		
		DocumentBuilderFactory objDocumentBuilderFactory = null;
		DocumentBuilder objDocumentBuilder = null;
		Document doc = null;

		try {

			objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
			objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();

			doc = objDocumentBuilder.parse(stream);

		} catch (Exception ex) {
			throw ex;
		}

		return doc;
	}
}