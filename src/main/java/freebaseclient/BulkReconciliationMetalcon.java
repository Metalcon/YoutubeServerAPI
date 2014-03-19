package freebaseclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

public class BulkReconciliationMetalcon {

	public static Properties properties = new Properties();

	public static void main(String[] args) {
		try {
			properties.load(new FileInputStream("freebase.properties"));
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport
					.createRequestFactory();
			JSONParser parser = new JSONParser();
			GenericUrl url = new GenericUrl("https://www.googleapis.com/rpc");
			// GenericUrl url = new
			// GenericUrl("https://www.googleapis.com/freebase/v1/reconcile");


			File outputFile = new File("Band_Freebase_matched.csv");

			
			ArrayList<String> bandListArray = new ArrayList<String>();
			String line;
			try {
				FileReader fileReader = new FileReader("Band_LITE.csv");
				BufferedReader bufferedReader = new BufferedReader(fileReader);

				for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {
					bandListArray.add(line);
					// System.out.println(line + " " + i);
				}
			} catch (IOException e) {
				System.out.println("Problem reading Bandlist!");
			}

			JSONArray requestBody = new JSONArray();

			for (int i = 0; i < bandListArray.size(); i++) {

				String bandDataColumn = bandListArray.get(i);
				String[] bandDataSplitArray = bandDataColumn.split("\t");
				System.out.println(bandDataSplitArray[1]);

				JSONObject requestBodyContent = new JSONObject();
				// prepare request metadata
				requestBodyContent.put("jsonrpc", "2.0");

				// TODO: find out what to put here
				requestBodyContent.put("id", i);
				requestBodyContent.put("method", "freebase.reconcile");
				requestBodyContent.put("apiVersion", "v1");

				JSONObject exampleRequestContent = new JSONObject();
				exampleRequestContent.put("name", bandDataSplitArray[1]);

				JSONArray exampleRequestContentKind = new JSONArray();
				exampleRequestContentKind.add("/music/artist");
				exampleRequestContent.put("type", exampleRequestContentKind);

				JSONArray exampleRequestEmpty = new JSONArray();

				// to request certain kinds of content, simply write it as a key
				// with null Value
				// exampleRequestContent.put("/music/album",
				// exampleRequestEmpty);
				JSONArray exampleRequestContentProp = new JSONArray();
				exampleRequestContentProp
						.add("/music/artist/genre:Heavy metal");
				exampleRequestContent.put("prop", exampleRequestContentProp);

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

			// TODO: parse response (JSONArray to String)
			HttpResponse httpResponse = request.execute();

			JSONArray response = new JSONArray();
			JSONParser jsonparser = new JSONParser();
			response = (JSONArray) jsonparser.parse(httpResponse
					.parseAsString());

			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			String outputString = "";
			
			for (int j = 0; j < response.size(); j++) {
				JSONObject responseEntry = (JSONObject) jsonparser
						.parse(response.get(j).toString());
				JSONObject responseEntryResult = (JSONObject) responseEntry
						.get("result");
				JSONArray responseEntryResultCandidates = (JSONArray) responseEntryResult
						.get("candidate");

				/* a single entry can have multiple candidates
				 * It seems like the candidates are sorted by their confidence score
				 * TODO: look up whether this is reliable
				 *  
				 */
				for (Object candidate : responseEntryResultCandidates) {
					outputString += (bandListArray.get(j) + "\t" + JsonPath.read(candidate, "$.mid")
							.toString()
							+ "\t"
							+ JsonPath.read(candidate, "$.confidence").toString()
							+  "\n");
					
					
					//remove this break if you want to get more than one result!
					break;
				}

			}				System.out.println(outputString);
			bufferedWriter.write(outputString);
			bufferedWriter.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
