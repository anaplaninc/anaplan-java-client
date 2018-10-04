//   Copyright 2011, 2012 Anaplan Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.anaplan.client;

import com.anaplan.client.dto.TaskResultDetailData;
import com.anaplan.client.ex.InvalidTaskResultDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Detail part of result of a task completing.
 */
public class TaskResultDetail {
    private static final Logger LOG = LoggerFactory.getLogger(TaskResultDetail.class);
    private TaskResultDetailData data;
    private Map<String, String> values;

    TaskResultDetail(TaskResultDetailData data) {
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            return appendTo(new StringBuilder()).toString();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    /**
     * Write a textual representation of this task result.
     *
     * @param out the object to receive the formatted output
     * @return The out parameter passed to this method
     * @since 1.3
     */
    public Appendable appendTo(Appendable out) throws IOException {
        if (data.getLocalMessageText() != null) {
            out.append(data.getLocalMessageText());
        }
        if (data.getOccurrences() != 0) {
            out.append(": ").append(Integer.toString(data.getOccurrences()));
        }
        if (getValues() != null && getValues().size() != 0) {     // jbackes 10/08/2018 - Add null check
            out.append("\n");
            getValues().forEach((k, v) -> {
                try {
                    if (k.equals("serverAlert") && v == null) {
                        v = "Completed successfully!";
                    }
                    out.append(k).append(" - ").append(v).append("\n");
                } catch (IOException e) {
                    throw new InvalidTaskResultDetail(e);
                }
            });
        } else {
            LOG.debug("TaskResultDetail:AppendTo out =" + out + ", getValues =" + getValues());
        }
        return out;
    }

    /**
     * Get the type of detail.
     * This is useful for programmatically checking for result information.
     */
    public String getType() {
        return data.getType();
    }

    /**
     * Get any additional information as key/value pairs.
     * This is useful for programmatically checking for result information.
     */
    public Map<String, String> getValues() {
        if (data.getValues() != null && values == null) {
            Map<String, String> newValues = new LinkedHashMap<String, String>();
            Iterator<String> i = data.getValues().iterator();
            while (i.hasNext()) {
                String key = i.next();
                if (i.hasNext())
                    newValues.put(key, i.next());
            }
            values = Collections.unmodifiableMap(newValues);
        } else {
            LOG.debug("TaskResultDetail:getValues data.getValues() =" + data.getValues() + ", values =" + values);
        }
        return values;
    }

    /**
     * Get the localized message text.
     * Returns a localized version of the message if available on the server,
     * otherwise the message will be in English.
     * Any additional information is incorporated into the message.
     */
    public String getLocalizedMessageText() {
        return data.getLocalMessageText();
    }

    /**
     * Get the number of occurrences.
     * If multiple occurrences of the same detail are aggregated together
     * in the result, this contains the number of occurrences. Otherwise zero
     * will be returned.
     */
    public int getOccurrences() {
        return data.getOccurrences();
    }
}
