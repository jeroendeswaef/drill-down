package service;

import java.util.Map;
import org.apache.solr.client.solrj.response.RangeFacet;

/**
 *
 * @author Jeroen De Swaef <j@recallq.com>
 */
public interface TimeAxis {

    long getEnd();

    long getGap();

    long getStart();

    Map<String, Integer> getValues(RangeFacet rangeFacet);

}
