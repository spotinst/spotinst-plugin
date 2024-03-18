package hudson.plugins.spotinst.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by ItayShklar on 07/08/2023.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseStatefulInstance {
    //region members
    private String                    id;
    //endregion

    //region getters & setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    //endregion
}

