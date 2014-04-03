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

/**
 * Implementation for Freebase request supported by Metalcon
 * 
 * @author Christian Schowalter
 * 
 */
public class MetalconFreebaseRequest implements api.FreebaseRequest {

	// TODO: the methods get the API-Key from a file on disc. Check if this
	// makes sense!
	private static Properties properties = new Properties();

	/**
	 * Bulk reconcilation
	 * 
	 * @return a list containing the meta data that could be obtained. If no mid
	 *         was found, the mid field will be null and the confidence 0.
	 * @throws IOException
	 *             , ParseException
	 */
	public List<FreebaseMetaData> reconcileBands(List<String> bands)
			throws IOException, ParseException {
		final int maximalQueryLength = 100;

		List<FreebaseMetaData> freeBaseMetaDataBands = new ArrayList<FreebaseMetaData>();

		if (bands.size() > maximalQueryLength) {
			for (int i = 0; i < bands.size(); i += maximalQueryLength - 1) {
				if (i + maximalQueryLength - 1 < bands.size()) {
					List<String> bandListPart = bands.subList(i, i
							+ maximalQueryLength - 1);
					HttpResponse httpResponse = buildReconciliationRequest(
							bandListPart).execute();
					freeBaseMetaDataBands.add(parseResponse(httpResponse,
							bandListPart));
				} else {
					List<String> bandListPart = bands.subList(i, bands.size());
					HttpResponse httpResponse = buildReconciliationRequest(
							bandListPart).execute();
					freeBaseMetaDataBands.add(parseResponse(httpResponse,
							bandListPart));
				}

			}
		} else {
			HttpResponse httpResponse = buildReconciliationRequest(bands)
					.execute();
			freeBaseMetaDataBands.add(parseResponse(httpResponse, bands));
		}
		return freeBaseMetaDataBands;
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
				freebaseMetaDataEntry.setConfidence(0.0);
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

	private HttpRequest buildReconciliationRequest(List<String> bandListPart)
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

	/**
	 * Single reconcilation
	 * 
	 * @return a String containing the meta data that could be obtained. If no
	 *         mid was found, the mid field will be null and the confidence 0.
	 * @throws IOException
	 *             , ParseException
	 */
	public FreebaseMetaData reconcileBand(String bandname) throws IOException,
			ParseException {
		FreebaseMetaData freebaseMetaDataBand = new FreebaseMetaData();
		HttpResponse httpResponse = buildReconciliationRequest(bandname)
				.execute();
		FreebaseMetaData response = parseResponse(httpResponse,
				freebaseMetaDataBand);
		return response;
	}

	private HttpRequest buildReconciliationRequest(String bandname)
			throws IOException, ParseException {
		try {
			properties.load(new FileInputStream("freebase.properties"));
		} catch (IOException e1) {
			System.out.println("Problem reading properties!");
			e1.printStackTrace();
		}

		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/freebase/v1/reconcile");
		url.put("name", bandname);
		url.put("kind", "/music/artist");
		url.put("prop", "/music/artist/genre:Heavy metal");
		url.put("key", properties.get("API_KEY"));
		HttpRequest request = requestFactory.buildGetRequest(url);

		return request;
	}

	private FreebaseMetaData parseResponse(HttpResponse httpResponse,
			FreebaseMetaData result) throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		JSONObject response = (JSONObject) jsonParser.parse(httpResponse
				.parseAsString());
		JSONArray candidates = (JSONArray) response.get("candidate");
		if (candidates == null) {
			result.setConfidence(0.0);
		} else {
			for (int j = 0; j < response.size(); j++) {
				JSONObject responseEntry = (JSONObject) jsonParser
						.parse(response.get(j).toString());

				result.setMid(JsonPath.read(responseEntry.get(0), "$.mid")
						.toString());
				result.setConfidence(Double.parseDouble(JsonPath.read(
						responseEntry.get(0), "$.confidence").toString()));
			}

		}
		return result;
	}

	/**
	 * @return a list containing the name and mids of the bands records
	 * @param a
	 *            Metabase-ID of a music band
	 */
	public List<FreebaseMetaData> retrieveRecordsForFreebaseBand(String bandMid)
			throws IOException, ParseException {
		List<FreebaseMetaData> freeBaseMetaDataRecords = new ArrayList<FreebaseMetaData>();
		HttpResponse httpResponse = buildSearchRequest(bandMid).execute();

		freeBaseMetaDataRecords = parseRecordSearchResponse(httpResponse,
				freeBaseMetaDataRecords);
		return freeBaseMetaDataRecords;
	}

	private List<FreebaseMetaData> parseRecordSearchResponse(
			HttpResponse httpResponse,
			List<FreebaseMetaData> freeBaseMetaDataRecords)
			throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		JSONObject responseJson = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		JSONArray resultContainer = (JSONArray) responseJson.get("result");
		JSONArray resultContainerCategory = JsonPath.read(resultContainer,
				"$./music/artist/album");
		for (Object entry : resultContainerCategory) {
			FreebaseMetaData temp = new FreebaseMetaData();
			temp.setMid(JsonPath.read(entry, "$.mid").toString());
			temp.setConfidence(Double.parseDouble(JsonPath
					.read(entry, "$.name").toString()));
			freeBaseMetaDataRecords.add(temp);
		}

		return freeBaseMetaDataRecords;
	}

	private HttpRequest buildSearchRequest(String bandMid) throws IOException {
		try {
			properties.load(new FileInputStream("freebase.properties"));
		} catch (IOException e1) {
			System.out.println("Problem reading properties!");
			e1.printStackTrace();
		}
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/freebase/v1/mqlread");

		String query = "[{\"mid\":\""
				+ bandMid
				+ "\",\"/music/artist/album\":[{\"name\":null , \"mid\":null}]}]";
		url.put("query", query);
		url.put("key", properties.get("API_KEY"));
		HttpRequest request = requestFactory.buildGetRequest(url);

		return request;
	}

}
