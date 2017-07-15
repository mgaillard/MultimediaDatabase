package mdb;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class LireSolrResultList {
    private List<LireSolrResult> results;

    private int cursor;

    public LireSolrResultList(List<LireSolrResult> results) {
        this.results = results;
        this.cursor = 0;
    }

    public List<LireSolrResult> getResults() {
        return results;
    }

    public int getCursor() { return cursor; }

    public void setCursor(int cursor) { this.cursor = cursor; }

    public void ThresholdResults(BigDecimal min, BigDecimal max) {
        for (Iterator<LireSolrResult> it = results.listIterator(); it.hasNext(); ) {
            LireSolrResult res = it.next();

            if (res.getDistance().compareTo(min) < 0 || res.getDistance().compareTo(max) > 0) {
                it.remove();
            }
        }
    }

    public LireSolrResult SearchByRid(String rowid) {
        LireSolrResult result = null;

        for (Iterator<LireSolrResult> it = results.listIterator(); it.hasNext() && result == null; ) {
            LireSolrResult res = it.next();

            if (rowid.equals(res.getRowid())) {
                result = res;
            }
        }

        return result;
    }
}
