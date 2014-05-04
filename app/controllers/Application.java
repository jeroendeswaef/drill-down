package controllers;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.*;
import play.libs.Json;
import play.mvc.*;

import service.SearchResultsService;
import service.TimeRangeType;
import views.html.*;

import com.google.gson.*;

/**
 * @author Jeroen De Swaef <j@recallq.com>
 */
public class Application extends Controller {

    public static Result index() {
    	Map<String, Object> attributes = new HashMap<String, Object>();

    	SearchResultsService service = getSearchService();
            
        // default values
        Map<String, Object> results = service.getResults(new String(), TimeRangeType.LAST_WEEK);
           
        for (Map.Entry<String, Object> resultEntry: results.entrySet()) {
            if (resultEntry instanceof JsonElement) {
                Gson gson = new Gson();
                String jsonStr = gson.toJson((JsonElement) resultEntry);
                attributes.put(resultEntry.getKey(), jsonStr);
            } else {
            	attributes.put(resultEntry.getKey(), resultEntry.getValue());
            }
        }

        return ok(index.render(attributes));
    }

    public static Result search() {
        SearchResultsService service = getSearchService();

        TimeRangeType selectedTimeRange = TimeRangeType.LAST_MONTH;
        String requestLike = new String();

        Http.RequestBody body = request().body();

        Map<String, String[]> postParams = body.asFormUrlEncoded();
        String[] timeRangeValues = postParams.get("timeRange");
        if (timeRangeValues.length > 0) {
            selectedTimeRange = TimeRangeType.valueOf(timeRangeValues[0]);
        }

        String[] requestLikeValues = postParams.get("requestLike");
        if (requestLikeValues.length > 0) {
            requestLike = requestLikeValues[0];
        }
        Map<String, Object> results = service.getResults(requestLike, selectedTimeRange);

        JsonObject returnObj = new JsonObject();

        for (Map.Entry<String, Object> resultEntry : results.entrySet()) {

            if (resultEntry.getValue() instanceof JsonElement) {

                returnObj.add(resultEntry.getKey(), (JsonElement) resultEntry.getValue());

            } else if (resultEntry.getValue() instanceof Long) {
                returnObj.addProperty(resultEntry.getKey(), (Long) resultEntry.getValue());
            }
        }
        Gson gson = new Gson();
        String jsonStr = gson.toJson(returnObj);

        JsonNode result = Json.parse(jsonStr);

        return ok(result);
    }

    private static SearchResultsService getSearchService() {
        Configuration conf = Play.application().configuration();
        String solrUsername = conf.getString("solr.username");
        String solrPassword = conf.getString("solr.password");
        String solrUrl = conf.getString("solr.url");
        SearchResultsService service = SearchResultsService.getInstance(solrUsername, solrPassword, solrUrl);
        return service;
    }
}
