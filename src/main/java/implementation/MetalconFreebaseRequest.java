package implementation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import api.FreebaseMetaData;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

public class MetalconFreebaseRequest implements api.FreebaseRequest {

	public static Properties properties = new Properties();

	public List<FreebaseMetaData> reconcileBands(List<String> bands)
			throws IOException, ParseException {
		final int maximalQueryLength = 100;

		List<FreebaseMetaData> freeBaseMetaData = new ArrayList<FreebaseMetaData>();

		if (bands.size() > maximalQueryLength) {
			for (int i = 0; i < bands.size(); i += maximalQueryLength - 1) {
				if (i + maximalQueryLength - 1 < bands.size()) {
					List<String> bandListPart = bands.subList(i, i
							+ maximalQueryLength - 1);
					HttpResponse httpResponse = BuildRequest(bandListPart)
							.execute();
					freeBaseMetaData.add(parseResponse(httpResponse,
							bandListPart));
				} else {
					List<String> bandListPart = bands.subList(i, bands.size());
					HttpResponse httpResponse = BuildRequest(bandListPart)
							.execute();
					freeBaseMetaData.add(parseResponse(httpResponse,
							bandListPart));
				}

			}
		} else {
			HttpResponse httpResponse = BuildRequest(bands).execute();
			freeBaseMetaData.add(parseResponse(httpResponse, bands));
		}
		return freeBaseMetaData;
	}

	private FreebaseMetaData parseResponse(HttpResponse httpResponse,
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
		FreebaseMetaData freebaseMetaDataEntry = new FreebaseMetaData();
		boolean candidateFound;
		for (int j = 0; j < response.size(); j++) {
			JSONObject responseEntry = (JSONObject) jsonparser.parse(response
					.get(j).toString());
			JSONObject responseEntryResult = (JSONObject) responseEntry
					.get("result");
			JSONArray responseEntryResultCandidates = (JSONArray) responseEntryResult
					.get("candidate");
			if (responseEntryResultCandidates == null) {
				System.out.println("null result received -->"
						+ bandListPart.get(j));
			} else {

				freebaseMetaDataEntry.setMid(JsonPath.read(
						responseEntryResultCandidates.get(0), "$.mid")
						.toString());
				String confidenceString = ((JsonPath.read(
						responseEntryResultCandidates.get(0),
						"$.confidence".toString())));
				freebaseMetaDataEntry.setConfidence(Double
						.parseDouble(confidenceString));
			}
		}
		return (freebaseMetaDataEntry);

	}

	private HttpRequest BuildRequest(List<String> bandListPart)
			throws IOException {

		try {
			properties.load(new FileInputStream("freebase.properties"));
		} catch (IOException e1) {
			System.out.println("Problem reading properties!");
			e1.printStackTrace();
		}
		GenericUrl url = new GenericUrl("https://www.googleapis.com/rpc");
		JSONArray requestBody = new JSONArray();
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		for (int i = 0; i < bandListPart.size(); i++) {

			String bandDataColumn = bandListPart.get(i);
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

		HttpRequest request = requestFactory.buildPostRequest(url,
				ByteArrayContent.fromString("application/json",
						requestBodyString));

		return request;
	}

	public FreebaseMetaData reconcileBand(String bandname) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<FreebaseMetaData> retrieveRecordsForFreebaseBand(String mid) {
		// TODO Auto-generated method stub
		return null;
	}

}
