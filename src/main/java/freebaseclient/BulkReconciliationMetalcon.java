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
import org.omg.CORBA.RepositoryIdHelper;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

/**
 * This is an experimental implementation of bulk reconciliation for our metalcon database.
 * It extracts band names and asks Freebase for MIDs which are then written to a new file.
 * 
 * @author Christian Schowalter
 */
public class BulkReconciliationMetalcon {

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
		writeToFile(outputString);
	}

	// Freebase queries have a limit concerning the number of requested
	// entries per query so we have to divide large lists to severeal
	// requests.
	// TODO: implement request splitting

	private static List<String> readInputFile() {
		String line;
		List<String> bandListArray = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader("Band.csv");
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

	public static HttpRequest BuildRequest(List<String> bandListArray)
			throws IOException {

		GenericUrl url = new GenericUrl("https://www.googleapis.com/rpc");
		JSONArray requestBody = new JSONArray();
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		for (int i = 0; i < bandListArray.size(); i++) {

			String bandDataColumn = bandListArray.get(i);
			String[] bandDataSplitArray = bandDataColumn.split("\t");
			System.out.println(bandDataSplitArray[1]);

			JSONObject requestBodyContent = new JSONObject();
			// prepare request metadata
			requestBodyContent.put("jsonrpc", "2.0");

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
			exampleRequestContentProp.add("/music/artist/genre:Heavy metal");
			exampleRequestContent.put("prop", exampleRequestContentProp);

			// API-Key is needed for every single concept... wtf?!
			exampleRequestContent.put("key", properties.get("API_KEY"));

			requestBodyContent.put("params", exampleRequestContent);

			requestBody.add(requestBodyContent);
		}
		String requestBodyString = requestBody.toString();
		//System.out.println(requestBodyString);

		HttpRequest request = requestFactory.buildPostRequest(url,
				ByteArrayContent.fromString("application/json",
						requestBodyString));

		return request;
	}

	public static String parseResponse(HttpResponse httpResponse,
			List bandListArray) throws ParseException, IOException {

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
		boolean candidateFound;
		for (int j = 0; j < response.size(); j++) {
			candidateFound = false;
			JSONObject responseEntry = (JSONObject) jsonparser.parse(response
					.get(j).toString());
			JSONObject responseEntryResult = (JSONObject) responseEntry
					.get("result");
			JSONArray responseEntryResultCandidates = (JSONArray) responseEntryResult
					.get("candidate");
			if (responseEntryResultCandidates == null) {
				responseString += bandListArray.get(j) + "\t" + "NO_MID_FOUND"
						+ "\t" + "0.0" + "\n";
				System.out.println("null result received -->"
						+ bandListArray.get(j));
			} else {

				/*
				 * a single entry can have multiple candidates It seems like the
				 * candidates are sorted by their confidence score TODO: look up
				 * whether this is reliable
				 */

				for (Object candidate : responseEntryResultCandidates) {
					responseString += (bandListArray.get(j)
							+ "\t"
							+ JsonPath.read(candidate, "$.mid").toString()
							+ "\t"
							+ JsonPath.read(candidate, "$.confidence")
									.toString() + "\n");
					// remove this break if you want to get more than one
					// result!
					break;
				}
			}
		}

		System.out.println(responseString);
		return (responseString);

	}

	public static void writeToFile(String dataToSave) throws IOException {
		File outputFile = new File("Band_Freebase_matched.csv");
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(dataToSave);
		bufferedWriter.close();
	}
}
