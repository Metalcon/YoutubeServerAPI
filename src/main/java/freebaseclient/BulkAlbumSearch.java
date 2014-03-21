package freebaseclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * This class takes the next step after the reconciliation by asking Freebase for the music albums the bands each have in their freebase entry
 * @author Christian Schowalter
 */
public class BulkAlbumSearch {
	
	// TODO: find a good value for maximum number of requests per query
	private static final int maximalQueryLength = 100;
	public static Properties properties = new Properties();
	public static String outputString = null;

	public static void main(String[] args) throws IOException, ParseException {

		try {
			properties.load(new FileInputStream("freebase.properties"));
		} catch (IOException e1) {
			System.out.println("Problem reading properties!");
			e1.printStackTrace();
		}
		JSONParser parser = new JSONParser();
		List<String> bandListArray = readInputFile();
		findAlbums(bandListArray);
		
		//writeToFile(outputString);
	}
	
	private static void findAlbums(List<String> bandListArray) throws IOException, ParseException {
		if (bandListArray.size() > maximalQueryLength) {
			for (int i = 0; i < bandListArray.size(); i += maximalQueryLength - 1) {
				if (i + maximalQueryLength - 1 < bandListArray.size()) {
					List<String> bandListPart = bandListArray.subList(i, i
							+ maximalQueryLength - 1);
					HttpResponse httpResponse = BuildRequest(bandListPart)
							.execute();
					outputString += parseResponse(httpResponse, bandListPart);
				} else {
					List<String> bandListPart = bandListArray.subList(i,
							bandListArray.size());
					HttpResponse httpResponse = BuildRequest(bandListPart)
							.execute();
					outputString += parseResponse(httpResponse, bandListPart);
				}

			}
		} else {
			HttpResponse httpResponse = BuildRequest(bandListArray).execute();
			outputString = parseResponse(httpResponse, bandListArray);
		}
	}

	private static String parseResponse(HttpResponse httpResponse,
			List<String> bandListPart) throws ParseException, IOException {
		JSONArray response = new JSONArray();
		JSONParser jsonparser = new JSONParser();
		try {
			response = (JSONArray) jsonparser.parse(httpResponse
					.parseAsString());
			System.out.println(response.toString());
		} catch (ClassCastException ce) {
			System.err
					.println("Typecast failed. Response is probably broken. Can be caused by bad request");
		}
		String responseString = "";
		
		
		
		return null;
	}

	private static HttpRequest BuildRequest(List<String> requestArrayList) throws IOException {
		GenericUrl url = new GenericUrl("https://www.googleapis.com/rpc");
		JSONArray requestBody = new JSONArray();
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		for (int i = 0; i < requestArrayList.size(); i++) {
			String bandDataColumn = requestArrayList.get(i);
			String[] bandDataSplitArray = bandDataColumn.split("\t");
			System.out.println(bandDataSplitArray[2]);
			if (bandDataSplitArray[2].equals("NO_MID_FOUND")){break;}
			JSONObject requestBodyContent = new JSONObject();
			// prepare request metadata
			requestBodyContent.put("jsonrpc", "2.0");

			requestBodyContent.put("id", i);
			requestBodyContent.put("method", "freebase.reconcile");
			requestBodyContent.put("apiVersion", "v1");

			JSONObject exampleRequestContent = new JSONObject();
			exampleRequestContent.put("mid", bandDataSplitArray[2]);
			
			JSONArray exampleRequestContentKind = new JSONArray();
			//exampleRequestContentKind.add(null);
			exampleRequestContent.put("/music/artist/album", exampleRequestContentKind);
			
			// API-Key is needed for every single concept... wtf?!
			exampleRequestContent.put("key", properties.get("API_KEY"));

			requestBodyContent.put("params", exampleRequestContent);

			requestBody.add(requestBodyContent);
			
		}
		String requestBodyString = requestBody.toString();
		System.out.println(requestBodyString);

		HttpRequest request = requestFactory.buildPostRequest(url,
				ByteArrayContent.fromString("application/json",
						requestBodyString));

		return request;
	}

	private static List<String> readInputFile() {
		String line;
		List<String> bandListArray = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader("Band_Freebase_matched.csv");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {
				bandListArray.add(line);
			}
		} catch (IOException e) {
			System.err.println("Problem reading Bandlist!");
			e.printStackTrace();
		}
		return bandListArray;
	}
	
	public static void writeToFile(String dataToSave) throws IOException {
		File outputFile = new File("Band_Freebase_matched_Albums.csv");
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(dataToSave);
		bufferedWriter.close();
	}
}
