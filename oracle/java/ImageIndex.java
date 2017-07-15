package mdb;

import oracle.CartridgeServices.ContextManager;
import oracle.CartridgeServices.CountException;
import oracle.CartridgeServices.InvalidKeyException;
import oracle.ODCI.ODCIRidList;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;

import java.math.BigDecimal;
import java.sql.*;

public class ImageIndex implements SQLData {
    private BigDecimal key;

    // ---------- Implement SQLData interface ----------
    String sql_type;
    public String getSQLTypeName() throws SQLException
    {
        return sql_type;
    }

    public void readSQL(SQLInput stream, String typeName) throws SQLException
    {
        sql_type = typeName;
        key = stream.readBigDecimal();
    }

    public void writeSQL(SQLOutput stream) throws SQLException
    {
        stream.writeBigDecimal(key);
    }

    // ---------- Implementation of the ODCIIndex ----------
    private final static BigDecimal EXIT_SUCCESS = new BigDecimal(0);
    private final static BigDecimal EXIT_FAILURE = new BigDecimal(1);

    private final static BigDecimal SCNFLG_REGULAR_CALL = BigDecimal.valueOf(2);
    private final static BigDecimal SCNFLG_CLEANUP_CALL = BigDecimal.valueOf(1);

    public static java.math.BigDecimal ODCIIndexCreate(oracle.ODCI.ODCIIndexInfo info,
                                                       java.lang.String parms,
                                                       oracle.ODCI.ODCIEnv env) {
        // Delete all images in the index and commit
        boolean deleteResult = LireSolrApi.DeleteAllAndCommit();

        // The indexed table should be empty
        // Only one index is supported, it is not possible to index more than one table

        if (deleteResult) {
            return EXIT_SUCCESS;
        } else {
            return EXIT_FAILURE;
        }
    }

    public static java.math.BigDecimal ODCIIndexAlter(oracle.ODCI.ODCIIndexInfo info,
                                                      java.lang.String parms,
                                                      java.math.BigDecimal alterOption,
                                                      oracle.ODCI.ODCIEnv env) {
        FileLogger.log("ODCIIndexAlter not supported");
        return EXIT_FAILURE;
    }

    public static java.math.BigDecimal ODCIIndexDrop(oracle.ODCI.ODCIIndexInfo info,
                                                     oracle.ODCI.ODCIEnv env) {
        // Delete all images in the index and commit
        boolean deleteResult = LireSolrApi.DeleteAllAndCommit();

        if (deleteResult) {
            return EXIT_SUCCESS;
        } else {
            return EXIT_FAILURE;
        }
    }

    public static java.math.BigDecimal ODCIIndexInsert(oracle.ODCI.ODCIIndexInfo info,
                                                       java.lang.String rowid,
                                                       java.lang.String newval,
                                                       oracle.ODCI.ODCIEnv env) {
        BigDecimal return_code = EXIT_FAILURE;

        // Extract all features of the image for indexation in LireSolr
        String command = LireSolrApi.ExtractIndexImage(newval, rowid);

        if (command.length() > 0) {
            boolean addResult = LireSolrApi.ExecuteCommandAndCommit(command);

            if (addResult) {
                return_code = EXIT_SUCCESS;
            } else {
                FileLogger.log("Impossible to index the image " + newval);
            }
        } else {
            FileLogger.log("Impossible to extract features of the image d" + newval);
        }

        return return_code;
    }

    public static java.math.BigDecimal ODCIIndexUpdate(oracle.ODCI.ODCIIndexInfo info,
                                                       java.lang.String rowid,
                                                       java.lang.String oldval,
                                                       java.lang.String newval,
                                                       oracle.ODCI.ODCIEnv env) {
        FileLogger.log("ODCIIndexUpdate not supported");
        return EXIT_FAILURE;
    }

    public static java.math.BigDecimal ODCIIndexDelete(oracle.ODCI.ODCIIndexInfo info,
                                                       java.lang.String rowid,
                                                       java.lang.String oldval,
                                                       oracle.ODCI.ODCIEnv env) {
        boolean deleteResult = LireSolrApi.DeleteImageAndCommit(oldval);

        if (deleteResult) {
            return EXIT_SUCCESS;
        } else {
            return EXIT_FAILURE;
        }
    }

    public static java.math.BigDecimal ODCIIndexStart(STRUCT[] sctx,                  // The value of the scan context returned by some previous related query-time call (such as the corresponding ancillary operator, if invoked before the primary operator); NULL otherwise
                                                      oracle.ODCI.ODCIIndexInfo ia,   // Contains information about the index and the indexed column
                                                      oracle.ODCI.ODCIPredInfo op,    // Contains information about the operator predicate
                                                      oracle.ODCI.ODCIQueryInfo qi,   // Contains query information (hints plus list of ancillary operators referenced)
                                                      java.math.BigDecimal start,     // The start value of the bounds on the operator return value. The data type is identical to that of the operator's return value
                                                      java.math.BigDecimal stop,      // The stop value of the bounds on the operator return value. The data type is identical to that of the operator's return value.
                                                      java.lang.String image,         // The value arguments of the operator invocation. The number and data types of these arguments are identical to those of the value arguments to the operator.
                                                      oracle.ODCI.ODCIEnv env) {      // The environment handle passed to the routine
        BigDecimal return_code = EXIT_SUCCESS;
        // Minimum distance of the results
        BigDecimal min = new BigDecimal(0);
        // Maximum distance of the results
        BigDecimal max = new BigDecimal(Integer.MAX_VALUE);

        if (start != null) {
            // Assign the max value of start and min to min.
            min = start.max(min);
        }
        if (stop != null) {
            // Assign the min value of stop and max to max.
            max = stop.min(max);
        }

        LireSolrResultList results = LireSolrApi.QueryIndex(image);
        results.ThresholdResults(min, max);

        // register stored context with cartridge services
        try {
            int key = ContextManager.setContext(results);
            // create a ImageIndex instance and store the key in it
            Connection conn = DriverManager.getConnection("jdbc:default:connection:");
            Object[] impAttr = new Object[1];
            impAttr[0] = new BigDecimal(key);
            StructDescriptor sd = new StructDescriptor("IMAGEINDEX", conn);
            sctx[0] = new STRUCT(sd, conn, impAttr);
        } catch (CountException e) {
            FileLogger.log("ODCIIndexStart: CountException: " + e.getMessage());
            return_code = EXIT_FAILURE;
        } catch (SQLException e) {
            FileLogger.log("ODCIIndexStart: SQLException: " + e.getMessage());
            return_code = EXIT_FAILURE;
        }

        return return_code;
    }

    public java.math.BigDecimal ODCIIndexFetch(java.math.BigDecimal nrows,
                                               oracle.ODCI.ODCIRidList[] rids,
                                               oracle.ODCI.ODCIEnv env) {
        BigDecimal return_code = EXIT_SUCCESS;

        try {
            // retrieve stored context using the key
            LireSolrResultList results = (LireSolrResultList) ContextManager.getContext(key.intValue());
            // Number of results to return.
            int n = Math.min(nrows.intValue(), results.getResults().size());
            // Cursor on the result list
            int cursor = results.getCursor();
            // Results
            String[] rowids;

            // If there is less results than nrows, we add a null element.
            if (results.getResults().size() < nrows.intValue()) {
                rowids = new String[n + 1];
                // The last element has to be null to tell Oracle than there is no more results.
                rowids[n] = null;
            } else {
                rowids = new String[n];
            }

            // Fill the result table
            for (int i = 0; i < n; i++) {
                rowids[i] = results.getResults().get(cursor + i).getRowid();
            }
            rids[0] = new ODCIRidList(rowids);

            // Update the cursor for the next call of fetch.
            results.setCursor(cursor + n);
        } catch (InvalidKeyException e) {
            FileLogger.log("ODCIIndexFetch: InvalidKeyException: " + e.getMessage());
            return_code = EXIT_FAILURE;
        }

        return return_code;
    }

    public java.math.BigDecimal ODCIIndexClose(oracle.ODCI.ODCIEnv env) throws Exception {
        BigDecimal return_code = EXIT_SUCCESS;

        // Retrieve stored context using the key, and remove from ContextManager
        try {
            ContextManager.clearContext(key.intValue());
        } catch (InvalidKeyException e) {
            FileLogger.log("ODCIIndexClose: InvalidKeyException: " + e.getMessage());
            return_code = EXIT_FAILURE;
        }

        return return_code;
    }

    public static java.math.BigDecimal ImageDistance(java.lang.String ImageA,
                                                     java.lang.String ImageB,
                                                     oracle.ODCI.ODCIIndexCtx indexctx,
                                                     STRUCT[] scanctx,
                                                     java.math.BigDecimal scanflg) {
        BigDecimal response = BigDecimal.valueOf(-1);

        try {
        //RegularCall
        if (scanflg.compareTo(SCNFLG_REGULAR_CALL) == 0)
        {
            if (ImageA != null && ImageB != null)
            {
                LireSolrFeatureVector features_ImageB = null;

                // Extract the features of ImageB or read them in the context
                if (scanctx[0] != null) {
                    // Read the features in the context
                    if (scanctx[0].getAttributes() != null && scanctx[0].getAttributes().length > 0) {
                        BigDecimal key = (BigDecimal)scanctx[0].getAttributes()[0];
                        features_ImageB = (LireSolrFeatureVector) ContextManager.getContext(key.intValue());
                    } else {
                        FileLogger.log("ImageDistance: No attributes in the ScanContext " + scanctx[0].dump());
                    }
                } else {
                    // Extract the features with the API
                    features_ImageB = LireSolrApi.ExtractFeatureVector(ImageB);

                    // Save the features for latter use
                    int key = ContextManager.setContext(features_ImageB);
                    // create a ImageIndex instance and store the key in it
                    Connection conn = DriverManager.getConnection("jdbc:default:connection:");
                    Object[] impAttr = new Object[1];
                    impAttr[0] = new BigDecimal(key);
                    StructDescriptor sd = new StructDescriptor("IMAGEINDEX", conn);
                    scanctx[0] = new STRUCT(sd, conn, impAttr);
                }

                if (features_ImageB != null)
                {
                    BigDecimal distance = LireSolrApi.DistanceImageWithFeatureVector(ImageA, features_ImageB.getFeatureVector());
                    if (distance != null) {
                        response = distance;
                    } else {
                        FileLogger.log("ImageDistance: Impossible to compute the distance between " + ImageA + " and " + features_ImageB);
                    }
                }
                else
                {
                    FileLogger.log("ImageDistance: The feature vector extracted for " + ImageB + " is empty");
                }
            }
            else
            {
                FileLogger.log("ImageDistance: ImageA or ImageB is null");
            }
        }
        //CleanupCall
        else if (scanflg.compareTo(SCNFLG_CLEANUP_CALL) == 0)
        {
            // Clean the scan context
            if (scanctx[0] != null) {
                if (scanctx[0].getAttributes() != null) {
                    if (scanctx[0].getAttributes().length > 0) {
                        BigDecimal key = (BigDecimal) scanctx[0].getAttributes()[0];
                        ContextManager.clearContext(key.intValue());
                    }
                }
            }
        }
        // Unknown call
        else
        {
            FileLogger.log("ImageDistance: Error unknown ScanFlag " + scanflg);
        }

        } catch (CountException e) {
            FileLogger.log("ImageDistance: CountException: " + e.getMessage());
        } catch (SQLException e) {
            FileLogger.log("ImageDistance: SQLException: " + e.getMessage());
        } catch (InvalidKeyException e) {
            FileLogger.log("ImageDistance: InvalidKeyException: " + e.getMessage());
        }

        return response;
    }

    public static BigDecimal ImageScore(java.lang.String ImageA,
                                        java.lang.String ImageB,
                                        oracle.ODCI.ODCIIndexCtx indexctx,
                                        ImageIndex[] scanctx,
                                        java.math.BigDecimal scanflg) {
        BigDecimal response = BigDecimal.valueOf(-1);

        try {
            if (scanctx[0] != null && scanctx[0].key != null && indexctx != null) {
                // retrieve stored context using the key
                LireSolrResultList results = (LireSolrResultList) ContextManager.getContext(scanctx[0].key.intValue());
                LireSolrResult result = results.SearchByRid(indexctx.getRid());
                response = result.getDistance();
            }
        } catch (InvalidKeyException e) {
            FileLogger.log("ImageScore: InvalidKeyException: " + e.getMessage());
        } catch (SQLException e) {
            FileLogger.log("ImageScore: SQLException: " + e.getMessage());
        }

        return response;
    }
}
