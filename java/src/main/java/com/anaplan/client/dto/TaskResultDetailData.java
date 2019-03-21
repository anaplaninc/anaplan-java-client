package com.anaplan.client.dto;

import java.util.List;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/21/17
 * Time: 3:38 PM
 */
public class TaskResultDetailData {

    private String type;
    private List<String> values;
    private String localMessageText;
    private int occurrences;

    public String getType() {
        return type;
    }

    public List<String> getValues() {
        return values;
    }

    public String getLocalMessageText() {
        return localMessageText;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void setLocalMessageText(String localMessageText) {
        this.localMessageText = localMessageText;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }
}
