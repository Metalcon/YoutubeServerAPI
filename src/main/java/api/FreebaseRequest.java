package api;

import java.util.List;

/**
 * Interface for freebase requests which are supported by metalcon
 * 
 * @author rpickhardt
 * 
 */
public interface FreebaseRequest {

    /**
     * retrieves a list of Freebase concepts for a list of given strings
     * This should be handled via a bulk request.
     * 
     * @param bands
     *            is a list of strings
     * @return list of concepts from freebase which have been successfully
     *         reconciled
     */
    List<FreebaseMetaData> reconcileBands(List<String> bands);

    /**
     * makes an API request to freebase with a given bandname and retrieve the
     * most probable metalband as a freebase concept
     * 
     * @param bandname
     * @return a concept from freebase when reconciliation was possible and null
     *         otherwise
     */
    FreebaseMetaData reconcileBand(String bandname);

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
