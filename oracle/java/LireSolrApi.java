package mdb;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;

public class LireSolrApi {
    private final static String SOLR_URL = "http://liresolr:8983/solr/lire";
    private final static String INDEXER_URL = "http://lire-web-api:8080/lire-web-api/features";
    private final static String CHARSET = "UTF-8";
    private final static String DESCRIPTOR = "cl";

    private static String QueryUrl(String url) {
        StringBuilder result = new StringBuilder();

        try {
            URL query_url = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)query_url.openConnection();
            connection.setRequestProperty("Accept-Charset", CHARSET);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
                result.append("\n");
            }
            in.close();
        } catch (MalformedURLException e) {
            FileLogger.log("MalformedURL");
        } catch (IOException e) {
            FileLogger.log("IOException during LireSolr query: " + e.getMessage());
        }

        return result.toString();
    }

    public static LireSolrResultList QueryIndex(String img_url) {
        ArrayList<LireSolrResult> result_list = new ArrayList<LireSolrResult>();

        try {
            String query = String.format(SOLR_URL + "/lireq?wt=xml&ms=false&fl=*&field=" + DESCRIPTOR + "&url=%s", URLEncoder.encode(img_url, CHARSET));
            String xml_result = QueryUrl(query);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            InputSource source = new InputSource(new StringReader(xml_result));
            Node docs = (Node)xpath.evaluate("/response/arr[@name=\"docs\"]", source, XPathConstants.NODE);

            // Iterate over all documents
            Node doc = docs.getFirstChild();
            while (doc.getNextSibling() != null) {
                doc = doc.getNextSibling();
                if (doc.getNodeName().equals("lst")) {
                    String id = null;
                    String title = null;
                    BigDecimal distance = null;

                    // Extract the id, title and distance for each result.
                    Node doc_child = doc.getFirstChild();
                    while (doc_child.getNextSibling() != null) {
                        doc_child = doc_child.getNextSibling();
                        if (doc_child.getNodeType() == Node.ELEMENT_NODE) {
                            Element doc_child_el = (Element)doc_child;
                            if (doc_child_el.getAttribute("name").equals("d")) {
                                distance = new BigDecimal(doc_child_el.getFirstChild().getTextContent());
                                distance = distance.setScale(3, RoundingMode.HALF_UP);
                            } else if(doc_child_el.getAttribute("name").equals("title")) {
                                title = doc_child_el.getFirstChild().getTextContent();
                            } else if (doc_child_el.getAttribute("name").equals("id")) {
                                id = doc_child_el.getFirstChild().getTextContent();
                            }
                        }
                    }

                    if (id != null && title != null && distance != null) {
                        LireSolrResult result = new LireSolrResult(id, title, distance);
                        result_list.add(result);
                    }
                }
            }
        } catch(XPathExpressionException e) {
            FileLogger.log("XPathExpressionException during the query to the index: " + img_url);
        } catch (UnsupportedEncodingException e) {
            FileLogger.log("UnsupportedEncodingException during the query to the index: " + img_url);
        }

        return new LireSolrResultList(result_list);
    }

    /**
     * Use the LireSolr API to extract a feature vector from an image by URL.
     * ColorLayout (from MPEG-7) descriptor is used.
     * @param img_url Url of the image
     * @return The feature vector extracted from the image
     */
    public static LireSolrFeatureVector ExtractFeatureVector(String img_url) {
        String feature_vector = null;

        try {
            String query = String.format(SOLR_URL + "/lireq?wt=xml&field=" + DESCRIPTOR + "&extract=%s", URLEncoder.encode(img_url, CHARSET));
            String xml_result = QueryUrl(query);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            InputSource source = new InputSource(new StringReader(xml_result));

            feature_vector = xpath.evaluate("/response/str[@name=\"histogram\"]/text()", source);
        } catch(XPathExpressionException e) {
            FileLogger.log("XPathExpressionException during the extraction of image: " + img_url);
        } catch (UnsupportedEncodingException e) {
            FileLogger.log("UnsupportedEncodingException during the extraction of image: " + img_url);
        }

        return new LireSolrFeatureVector(img_url, feature_vector);
    }

    /**
     * Use the LireSolr API to compare an image by URL with a feature vector.
     * ColorLayout (from MPEG-7) descriptor is used.
     * @param img_id Url of the image
     * @param feature_vector Feature vector of an image
     * @return The distance between the image and the feature vector
     */
    public static BigDecimal DistanceImageWithFeatureVector(String img_id, String feature_vector) {
        BigDecimal distance = null;

        try {
            String query = String.format(SOLR_URL + "/select?wt=xml&q=id:\"%s\"&fl=score:lirefunc(" + DESCRIPTOR + ",\"%s\")", URLEncoder.encode(img_id, CHARSET), URLEncoder.encode(feature_vector, CHARSET));
            String xml_result = QueryUrl(query);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            InputSource source = new InputSource(new StringReader(xml_result));

            Node root = (Node)xpath.evaluate("/", source, XPathConstants.NODE);
            String numFound = xpath.evaluate("/response/result[@name=\"response\"]/@numFound", root);
            if (numFound != null && numFound.equals("1")) {
                String str_distance = xpath.evaluate("/response/result[@name=\"response\"]/doc/double/text()", root);
                if (str_distance != null) {
                    distance = new BigDecimal(str_distance);
                    distance = distance.setScale(3, RoundingMode.HALF_UP);
                }
            } else {
                FileLogger.log("numFound(" + numFound + ") != 1 during the comparison between image: " + img_id + " and " + feature_vector);
            }
        } catch(XPathExpressionException e) {
            FileLogger.log("XPathExpressionException during the comparison between image: " + img_id + " and " + feature_vector + " : " + e.getMessage());
        } catch(NumberFormatException e) {
            FileLogger.log("NumberFormatException during the conversion of the score of image: " + img_id + " and " + feature_vector + " : " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            FileLogger.log("UnsupportedEncodingException during the comparison between image: " + img_id + " and " + feature_vector + " : " + e.getMessage());
        }

        return distance;
    }

    public static String ExtractIndexImage(String img_url, String rowid) {
        String result = "";

        try {
            String query = String.format(INDEXER_URL + "?img=%s&title=%s", URLEncoder.encode(img_url, CHARSET), URLEncoder.encode(rowid, CHARSET));
            result = QueryUrl(query);
        } catch (UnsupportedEncodingException e) {
            FileLogger.log("UnsupportedEncodingException during the extracting index data from: " + img_url + " with rowid " + rowid);
        }

        return result;
    }

    /**
     * Execute a XML command on the LireSolr API.
     * @param command The XML command to execute
     * @param commit True to commit directly after the command, false otherwise
     * @return True if the return code is 200, false otherwise
     */
    public static boolean ExecuteCommand(String command, boolean commit) {
        boolean success = true;

        String query = SOLR_URL + "/update";

        // Add the commit
        if (commit) {
            query += "?commit=true";
        }

        try {
            URL url = new URL(query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("Accept-Charset", CHARSET);

            // Send post request
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(command);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                success = false;

                // Read the output and log it
                StringBuilder response = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                FileLogger.log("ExecuteCommand response code: " + responseCode + "\nResponse:\n" + response.toString());
            }
        } catch (MalformedURLException e) {
            FileLogger.log("ExecuteCommand MalformedURLException:" + e.getMessage());
        } catch (IOException e) {
            FileLogger.log("ExecuteCommand IOException: " + e.getMessage());
        }

        return success;
    }

    /**
     * Execute a XML command on the LireSolr API and commit.
     * @param command The XML command to execute
     * @return True if the return code is 200, false otherwise
     */
    public static boolean ExecuteCommandAndCommit(String command) {
        return ExecuteCommand(command, true);
    }

    public static boolean DeleteAll(boolean commit) {
        return LireSolrApi.ExecuteCommand("<delete><query>*:*</query></delete>", commit);
    }

    public static boolean DeleteAllAndCommit() {
        return DeleteAll(true);
    }

    public static boolean DeleteImage(String img_url, boolean commit) {
        return LireSolrApi.ExecuteCommand("<delete><id>" + img_url + "</id></delete>", commit);
    }

    public static boolean DeleteImageAndCommit(String img_url) {
        return DeleteImage(img_url, true);
    }

    public static boolean Commit() {
        return LireSolrApi.ExecuteCommand("<commit/>", false);
    }
}
