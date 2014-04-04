package freebaseclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.places.Place;
import com.flickr4java.flickr.test.TestInterface;

/**
 * An experimental implementation of Flickr Queries. Aiming to request data
 * relevant to our Metalcon interests from Flickr.
 * 
 * @author Christian Schowalter
 * 
 */
public class flickrPhotoSearch {
	public static Properties properties = new Properties();

	public static void main(String[] args) throws IOException, FlickrException {
		try {
			properties.load(new FileInputStream("flickr.properties"));
		} catch (FileNotFoundException fnfe) {
			System.err.println("missing flickr properties");
		}
		Flickr flickr = new Flickr(properties.getProperty("API_KEY"),
				properties.getProperty("SECRET_KEY"), new REST());
		TestInterface testInterface = flickr.getTestInterface();
		Map<String, String> testMap = new HashMap<String, String>();
		testMap.put("hello", "World");

		List<Place> koblenzPlace = flickr.getPlacesInterface().find(
				"Koblenz, rheinland-pfalz");
		System.out.println(koblenzPlace);
		// List<Photo> photos = new PhotoList<Photo>();

		SearchParameters params = new SearchParameters();
		params.setPlaceId(koblenzPlace.get(0).getPlaceId());
		String[] tags = { "Druckluftkammer", "nativ" };
		params.setTags(tags);

		List<Photo> photos = flickr.getPhotosInterface().search(params, 100, 0);
		for (int i = 0; i < photos.size(); ++i) {
			System.out.println(photos.get(i).getLargeUrl());
		}
		// test.get(0).getPlaceId();

		// Collection<Element> testCollection = new ArrayList<Element>();
		// testCollection = testInterface.echo(testMap);
		// System.out.println(testCollection);
	}
}
