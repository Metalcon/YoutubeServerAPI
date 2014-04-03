package api;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;

/**
 * Interface for freebase requests which are supported by metalcon
 * 
 * @author rpickhardt
 * 
 */
public interface FreebaseRequest {

	/**
	 * retrieves a list of Freebase concepts for a list of given strings This
	 * should be handled via a bulk request.
	 * 
	 * @param bands
	 *            is a list of strings
	 * @return list of concepts from freebase which have been successfully
	 *         reconciled
	 * @throws IOException
	 * @throws ParseException
	 */
	List<FreebaseMetaData> reconcileBands(List<String> bands)
			throws IOException, ParseException;

	/**
	 * makes an API request to freebase with a given bandname and retrieve the
	 * most probable metalband as a freebase concept
	 * 
	 * @param bandname
	 * @return a concept from freebase when reconciliation was possible and null
	 *         otherwise
	 * @throws IOException
	 * @throws ParseException
	 */
	FreebaseMetaData reconcileBand(String bandname) throws IOException,
			ParseException;

	/**
	 * Given the freebase mid of a band this returns a list of all records as
	 * freebase concepts
	 * 
	 * @param mid
	 *            of a band
	 * @return list of records for the band
	 */
	List<FreebaseMetaData> retrieveRecordsForFreebaseBand(String mid);
}
