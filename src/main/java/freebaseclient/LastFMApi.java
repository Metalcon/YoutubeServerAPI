package freebaseclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import api.GeoLocation;
import api.LastFMEventMetaData;
import api.Venue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class LastFMApi {

	public static Properties properties = new Properties();

	public static void main(String[] args) throws FileNotFoundException,
			IOException, ParseException {
		String query = "ironmaiden";
		List<LastFMEventMetaData> eventContainer = new ArrayList<LastFMEventMetaData>();
		lastFmApiEventCall(query, eventContainer, 30, 0);
		System.out.println(eventContainer.get(0).getEventId());
		System.out.println(eventContainer.get(0).getArtists());
		System.out.println(eventContainer.get(1).getEventId());
		System.out.println(eventContainer.get(1).getArtists());
		System.out.println(eventContainer.get(0).getVenue().getCity());
		System.out.println(eventContainer.get(1).getVenue().getCity());
		System.out.println("street: "
				+ eventContainer.get(0).getVenue().getStreet());
		System.out.println("street: "
				+ eventContainer.get(1).getVenue().getStreet());
		System.out.println("startdate " + eventContainer.get(0).getStartDate());
		System.out.println("startdate " + eventContainer.get(1).getStartDate());
		System.out.println("enddate " + eventContainer.get(0).getEndDate());
		System.out.println("enddate " + eventContainer.get(0).getEndDate());
	}

	/**
	 * 
	 * @param bandName
	 *            defines the band you want to retrieve events for
	 * @param container
	 *            This is the container that the events are saved in
	 *            (LastFMEventMetaData format)
	 * @param maxResults
	 *            Defines the number of results you want to get (1-50)
	 * @param festivalsOnly
	 *            Can select to show only festivals (1 for only Festivals, 0 for
	 *            all events)
	 * @throws IOException
	 * @throws ParseException
	 * 
	 *             This method can look up events for one specific band. It can
	 *             retrieve between one and fifty results at the same time.
	 */

	public static void lastFmApiEventCall(String bandName,
			List<LastFMEventMetaData> container, int maxResults,
			int festivalsOnly) throws IOException, ParseException {
		properties.load(new FileInputStream("lastfm.properties"));
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl("http://ws.audioscrobbler.com/2.0/");
		url.put("method", "artist.getevents");
		url.put("artist", bandName);
		url.put("format", "json");
		url.put("limit", maxResults);
		url.put("autocorrect", "1");
		url.put("festivalsonly", festivalsOnly);
		url.put("api_key", properties.get("API_KEY"));
		System.out.println(url);
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		processingSearchResults(response, container);
	}

	/**
	 * 
	 * @param response
	 *            is the response produced in the common call to the api in the
	 *            method lastFmApiEventCall
	 * @param container
	 *            Is containing all informations extracted from the lastFM
	 *            database for the call of all events of one specific band.It is
	 *            stored in an ArrayList<LastFMEventMetaData>
	 * 
	 *            This method is a help method for the lastFMApiEventCall to
	 *            outsource the processing. Here the response JSON is beeing
	 *            read out and all relevant informations put into
	 *            LastFMEventMetaData objects
	 * 
	 */

	// TODO: case with maxResults=1 must be handled separately, because the JSON
	// has a different structure(results are missing a Array and is instead a
	// single JSONObject)

	private static void processingSearchResults(JSONObject response,
			List<LastFMEventMetaData> container) {
		JSONObject responseEvents = (JSONObject) response.get("events");
		System.out.println(responseEvents);
		JSONArray responseEventsEvent = (JSONArray) responseEvents.get("event");
		for (int i = 0; i < responseEventsEvent.size(); i++) {
			LastFMEventMetaData temp = new LastFMEventMetaData();
			JSONObject responseEventEntry = (JSONObject) responseEventsEvent
					.get(i);

			// fills the id field

			if (testsIfFilled(responseEventEntry, "id")) {
				temp.setEventId(Integer.parseInt(responseEventEntry.get("id")
						.toString()));
			}

			// fills the field Artists
			JSONObject responseEventsEventArtists = (JSONObject) responseEventEntry
					.get("artists");
			Object typeTest = responseEventsEventArtists.get("artist");
			List<String> tempArtistList = new ArrayList<String>();
			if (typeTest instanceof JSONArray) {
				JSONArray responseEventsEventArtistsArtist = (JSONArray) typeTest;
				if (!responseEventsEventArtistsArtist.isEmpty()) {
					for (int j = 0; j < responseEventsEventArtistsArtist.size(); j++) {
						tempArtistList.add(responseEventsEventArtistsArtist
								.get(j).toString());
					}
					temp.setArtists(tempArtistList);
				}
			} else {
				if (testsIfFilled(responseEventEntry, "artists"))
					tempArtistList.add(responseEventEntry.get("artists")
							.toString());
				temp.setArtists(tempArtistList);
			}

			// fills the field venue
			JSONObject responseEventEntryVenue = (JSONObject) responseEventEntry
					.get("venue");
			Venue venueTemp = new Venue();

			// fills the field VenueId
			if (testsIfFilled(responseEventEntryVenue, "id")) {
				venueTemp.setVenueId(responseEventEntryVenue.get("id")
						.toString());
			}
			// fills the field VenueName
			if (testsIfFilled(responseEventEntryVenue, "name")) {
				venueTemp.setVenueName(responseEventEntryVenue.get("name")
						.toString());
			}
			JSONObject responseEventEntryVenueLocation = (JSONObject) responseEventEntryVenue
					.get("location");
			JSONObject responseEventEntryVenueLocationGeo = (JSONObject) responseEventEntryVenueLocation
					.get("geo:point");

			// creates a geolocation for the venue from the type GeoLocation()
			GeoLocation geoTemp = new GeoLocation();

			if (testsIfFilled(responseEventEntryVenueLocationGeo, "geo:lat")) {
				geoTemp.setGeoLat(Double
						.parseDouble(responseEventEntryVenueLocationGeo.get(
								"geo:lat").toString()));
			}
			if (testsIfFilled(responseEventEntryVenueLocationGeo, "geo:long")) {
				geoTemp.setGeoLong(Double
						.parseDouble(responseEventEntryVenueLocationGeo.get(
								"geo:long").toString()));
			}

			// fills the city field
			if (testsIfFilled(responseEventEntryVenueLocation, "city")) {
				venueTemp.setCity(responseEventEntryVenueLocation.get("city")
						.toString());
			}
			// fills the county field
			if (testsIfFilled(responseEventEntryVenueLocation, "country")) {
				venueTemp.setCountry(responseEventEntryVenueLocation.get(
						"country").toString());
			}
			// fills the street field
			if (testsIfFilled(responseEventEntryVenueLocation, "street")) {
				venueTemp.setStreet(responseEventEntryVenueLocation.get(
						"street").toString());
			}
			// fills the postalcode field
			if (testsIfFilled(responseEventEntryVenueLocation, "postalcode")) {
				venueTemp.setPostalCode(responseEventEntryVenueLocation.get(
						"postalcode").toString());
			}
			// fills the website field
			if (testsIfFilled(responseEventEntryVenue, "url")) {
				venueTemp.setVenueWebsite(responseEventEntryVenue.get("url")
						.toString());
			}
			temp.setVenue(venueTemp);

			// fills the startDate field
			if (testsIfFilled(responseEventEntry, "startDate")) {
				temp.setStartDate(responseEventEntry.get("startDate")
						.toString());
			}

			// fills the endDate
			if (responseEventEntry.containsKey("endDate")) {
				temp.setEndDate(responseEventEntry.get("endDate").toString());
			}
			container.add(temp);
		}
	}

	/**
	 * 
	 * @param jsonTemp
	 * @param type
	 * @return
	 * 
	 *         This method tests if a specific field is filled or not. Needed
	 *         before filling the fields, otherwise the programm would get
	 *         NullPointerExceptions for empty fields. Returns true if the
	 *         JSONField contains a string, false if it is empty
	 */

	private static boolean testsIfFilled(JSONObject jsonTemp, String type) {
		if (jsonTemp.get(type).equals("")) {
			return false;
		}
		if (jsonTemp.get(type) != null) {
			return true;
		} else {
			return false;
		}
	}
}
