package mdb;

import java.math.BigDecimal;

public class LireSolrResult {
    private String url;
    private String rowid;
    private BigDecimal distance;

    public LireSolrResult(String url, String rowid, BigDecimal distance) {
        this.url = url;
        this.rowid = rowid;
        this.distance = distance;
    }

    public String getUrl() {
        return url;
    }

    public String getRowid() {
        return rowid;
    }

    public BigDecimal getDistance() {
        return distance;
    }
}
