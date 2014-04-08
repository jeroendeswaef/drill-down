package com.recallq.drilldown.ui.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.recallq.drilldown.ui.DashboardServlet;
import com.recallq.drilldown.ui.MonthTimeAxis;
import com.recallq.drilldown.ui.TimeAxis;
import com.recallq.drilldown.ui.WeekTimeAxis;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author Jeroen De Swaef <j@recallq.com>
 */
public class SearchResultsService {

    private static SearchResultsService instance;

    private SolrServer server;

    private static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        public void process(final HttpRequest request, final HttpContext context)
                throws HttpException, IOException {
            AuthState authState = (AuthState) context
                    .getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                CredentialsProvider credsProvider = (CredentialsProvider) context
                        .getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context
                        .getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                Credentials creds = credsProvider.getCredentials(new AuthScope(
                        targetHost.getHostName(), targetHost.getPort()));
                if (creds == null) {
                    throw new HttpException(
                            "No credentials for preemptive authentication");
                }
                authState.setAuthScheme(new BasicScheme());
                authState.setCredentials(creds);
            }

        }

    }

    private static SolrServer getSolrServer(String solrUsername, String solrPassword, String solrUrl) {
        // Providing a custom http client necessary to provide http authentication
        PoolingClientConnectionManager cxMgr = new PoolingClientConnectionManager(
                SchemeRegistryFactory.createDefault());
        cxMgr.setMaxTotal(100);
        cxMgr.setDefaultMaxPerRoute(20);
        DefaultHttpClient httpclient = new DefaultHttpClient(cxMgr);
        httpclient.addRequestInterceptor(
                new SearchResultsService.PreemptiveAuthInterceptor(), 0);

        HttpSolrServer server;
        if (solrUsername != null) {
            httpclient.getCredentialsProvider().setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(solrUsername, solrPassword));
            server = new HttpSolrServer(solrUrl, httpclient);
        } else {
            server = new HttpSolrServer(solrUrl);
        }

        server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
        server.setConnectionTimeout(15000); // 5 seconds to establish TCP
        server.setSoTimeout(5000);  // socket read timeout
        server.setFollowRedirects(false);  // defaults to false
        // allowCompression defaults to false.
        // Server side must support gzip or deflate for this to have any effect.
        server.setAllowCompression(true);
        return server;
    }

    private SearchResultsService(String solrUsername, String solrPassword, String solrUrl) {
        this.server = getSolrServer(solrUsername, solrPassword, solrUrl);
    }

    public static synchronized SearchResultsService getInstance(String solrUsername, String solrPassword, String solrUrl) {
        if (instance == null) {
            instance = new SearchResultsService(solrUsername, solrPassword, solrUrl);
        }
        return instance;
    }

    /**
     * Adds a property "children" to the root json object.
     * 
     * @param facetPivot
     * @param root
     * @return 
     */
    private void recursiveAddChildData(List<PivotField> facetPivot, JsonObject root) {
        JsonArray childElements = new JsonArray();
        root.add("children", childElements);
        for (PivotField pivotField : facetPivot) {
            JsonObject dataItem = new JsonObject();
            Object value = pivotField.getValue();
            if (value instanceof Long) {    
                dataItem.addProperty("name", (Long) value);
            } else {
                dataItem.addProperty("name", (String) value);
            }
            dataItem.addProperty("size", pivotField.getCount());
            childElements.add(dataItem);
            if (pivotField.getPivot() != null && pivotField.getPivot().size() > 0) {
                recursiveAddChildData(pivotField.getPivot(), dataItem);
            }
        }
    }

    public Map<String, Object> getResults(String requestSearchTerm, TimeRangeType timeRange) {
        TimeAxis timeAxis;
        if (timeRange == TimeRangeType.LAST_WEEK) {
            timeAxis = new WeekTimeAxis(new Date(), new SimpleDateFormat("dd MMM"));
        } else {
            timeAxis = new MonthTimeAxis(new Date(), new SimpleDateFormat("dd MMM"));
        }
        Map<String, Object> results = new HashMap<String, Object>();

        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("*:*")
            //.addFilterQuery("status:200")
                    .addNumericRangeFacet("time_local", timeAxis.getStart(), timeAxis.getEnd(), timeAxis.getGap())
                    .addFilterQuery("time_local:[" + timeAxis.getStart() + " TO *]")
                    .addFacetPivotField("status,request-cat")
                    //.addFacetPivotField("request-method,status")
                    .setFacetSort("index");
                    
            if (StringUtils.isNotEmpty(requestSearchTerm)) {
                query.addFilterQuery("request-url:*" + requestSearchTerm + "*");
            }
            QueryResponse rsp = server.query(query);

            SolrDocumentList docs = rsp.getResults();
            long totalCount = docs.getNumFound();
            
            results.put("totalCount", new Long(totalCount));
            
            JsonArray dataSets = new JsonArray();
            JsonObject dataSet = new JsonObject();
            dataSet.addProperty("key", "Cumulative Return");

            JsonArray values = new JsonArray();
            for (Map.Entry<String, Integer> histogramEntry : timeAxis.getValues(rsp.getFacetRanges().get(0)).entrySet()) {
                JsonObject value = new JsonObject();
                value.add("label", new JsonPrimitive(histogramEntry.getKey()));
                value.add("value", new JsonPrimitive(histogramEntry.getValue()));
                values.add(value);
            }
            dataSet.add("values", values);
            dataSets.add(dataSet);

            //Iterator<Map.Entry<String, List<PivotField>>> it = rsp.getFacetPivot().iterator();
            //while(it.hasNext()) {
            //    System.out.println("f>>" + it.next().getKey());
            //}
            JsonObject sunBurstData = new JsonObject();
            sunBurstData.addProperty("name", "root");

            List<PivotField> facetPivot = rsp.getFacetPivot().get("status,request-cat");
            recursiveAddChildData(facetPivot, sunBurstData);
            /*JsonArray sunBustDataChildren = new JsonArray();
            List<PivotField> facetPivot = rsp.getFacetPivot().get("status,request-method");
            for (PivotField pivotField : facetPivot) {
                JsonObject dataItem = new JsonObject();
                dataItem.addProperty("name", (Long) pivotField.getValue());
                dataItem.addProperty("size", pivotField.getCount());
                System.out.println(">>pf:" + pivotField.getCount() + " for " + (Long) pivotField.getValue());
                for (PivotField pivotFieldChild : pivotField.getPivot()) {
                    System.out.println("   >>pf:" + pivotFieldChild.getCount());
                }
                sunBustDataChildren.add(dataItem);
            }
            sunBurstData.add("children", sunBustDataChildren);
            */
            
            results.put("requestCountData", dataSets);
            results.put("sunBurstData", sunBurstData);
            results.put("query", query.toString());

        } catch (SolrServerException ex) {
            Logger.getLogger(DashboardServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }
}
