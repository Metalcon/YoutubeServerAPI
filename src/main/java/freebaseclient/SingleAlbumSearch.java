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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class SingleAlbumSearch {
	public static Properties properties = new Properties();

	public static void main(String[] args) {
		try {
			properties.load(new FileInputStream("freebase.properties"));
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport
					.createRequestFactory();
			JSONParser parser = new JSONParser();
			GenericUrl url = new GenericUrl(
					"https://www.googleapis.com/freebase/v1/mqlread");

			// TODO: implement reading mid from file
			String bandMid = "/m/014_xj\"";

			String query = "[{\"mid\":\""
					+ bandMid
					+ "\",\"/music/artist/album\":[{\"name\":null , \"mid\":null}]}]";
			url.put("query", query);

			System.out.println("my API-Key: " + properties.get("API_KEY"));
			url.put("key", properties.get("API_KEY"));
			HttpRequest request = requestFactory.buildGetRequest(url);
			System.out.println(url);
			HttpResponse httpResponse = request.execute();
			JSONObject response = (JSONObject) parser.parse(httpResponse
					.parseAsString());
			JSONArray candidates = (JSONArray) response.get("candidate");
			// for (Object candidate : candidates) {
			// System.out.print(JsonPath.read(candidate,"$.mid").toString()+" | "
			// + JsonPath.read(candidate,"$.name").toString()+" | " +
			// JsonPath.read(candidate,"$.notable.name").toString() + " (");
			// System.out.println(JsonPath.read(candidate,"$.confidence").toString()+")");
			// }
			System.out.println(response.toString());
			// TODO: implement extracting information (and writing to file)
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static List<String> readInputFile(String inputFile) {
		String line;
		List<String> listArray = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(inputFile + ".csv");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {
				listArray.add(line);
			}
		} catch (IOException e) {
			System.err.println("Problem reading List!");
			e.printStackTrace();
		}
		return listArray;
	}

	public static void writeToFile(String dataToSave, String saveFileName)
			throws IOException {
		File outputFile = new File(saveFileName + ".csv");
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(dataToSave);
		bufferedWriter.close();
	}
}
