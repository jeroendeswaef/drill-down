package com.recallq.drilldown.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.solr.client.solrj.response.RangeFacet;

/**
 *
 * @author Jeroen De Swaef <j@recallq.com>
 */
public class MonthTimeAxis implements TimeAxis {
    
    private final Calendar endCal;
    private final Calendar startCal;
    private final SimpleDateFormat df;
    
    public MonthTimeAxis(Date startDate, SimpleDateFormat df) {
        this.df = df;
        
        startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.add(Calendar.DATE, -30);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        endCal = Calendar.getInstance();
        endCal.setTime(startDate);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);
    }
    
    @Override
     public long getStart() {
        return startCal.getTime().getTime();
    }

    @Override
    public long getEnd() {
        return endCal.getTime().getTime();
    }

    @Override
    public long getGap() {
        return (3600000L * 24);
    }
    
    @Override
    public Map<String, Integer> getValues(RangeFacet rangeFacet) {
        Map<String, Integer> results = new LinkedHashMap<String, Integer>();
        for (Object histogramItem: rangeFacet.getCounts()) {
            RangeFacet.Count count = (RangeFacet.Count) histogramItem;
            Long millisecs = Long.parseLong(count.getValue());
            String key = df.format(new Date(millisecs));
            Integer value = count.getCount();
            results.put(key, value);
        }
        return results;
    }
}
