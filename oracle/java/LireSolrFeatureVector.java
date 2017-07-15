package mdb;

public class LireSolrFeatureVector {
    private String image;
    private String feature_vector;

    public LireSolrFeatureVector(String image, String feature_vector) {
        this.image = image;
        this.feature_vector = feature_vector;
    }

    public String getImage() {
        return image;
    }

    public String getFeatureVector() {
        return feature_vector;
    }
}
