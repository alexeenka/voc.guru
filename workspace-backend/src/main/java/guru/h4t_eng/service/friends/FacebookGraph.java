package guru.h4t_eng.service.friends;

import com.google.gson.annotations.SerializedName;

/**
 * FacebookGraph.
 *
 * Created by aalexeenka on 21.11.2016.
 */
public class FacebookGraph {

    @SerializedName("context")
    private FacebookGraphContext graphContext;

    public FacebookGraphContext getGraphContext() {
        return graphContext;
    }

    public void setGraphContext(FacebookGraphContext graphContext) {
        this.graphContext = graphContext;
    }
}
