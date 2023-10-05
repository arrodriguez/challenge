package com.muun.testutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.muun.core.VersionID;

public class EventResponseTestUtil {
    public static String mockEventResponse() {
        StringBuilder jsonBuilder = new StringBuilder();

        jsonBuilder.append("[");
        jsonBuilder.append("{");
        jsonBuilder.append("\"id\":\"32322495816\",");
        jsonBuilder.append("\"type\":\"PushEvent\",");
        jsonBuilder.append("\"payload\":{");
        jsonBuilder.append("\"commits\": [{");
        jsonBuilder.append("\"sha\":\"f36df489556ab499d4d0b3e8e22584f05c98f02e\",");
        jsonBuilder.append("\"author\":{");
        jsonBuilder.append("\"email\":\"arhrodriguez@gmail.com\",");
        jsonBuilder.append("\"name\":\"Ariel Horacio Rodriguez\"");
        jsonBuilder.append("},");
        jsonBuilder.append("\"message\":\"Automatic update\",");
        jsonBuilder.append("\"distinct\":true,");
        jsonBuilder.append("\"url\":\"https://api.github.com/repos/arrodriguez/losiunicos/commits/f36df489556ab499d4d0b3e8e22584f05c98f02e\"");
        jsonBuilder.append("}]");  // Close the commits array
        jsonBuilder.append("}");   // Close the payload object
        jsonBuilder.append("},");  // Close event object
        jsonBuilder.append("{");
        jsonBuilder.append("\"id\":\"32322495812\",");
        jsonBuilder.append("\"type\":\"PushEvent\",");
        jsonBuilder.append("\"payload\":{");
        jsonBuilder.append("\"commits\": [{");
        jsonBuilder.append("\"sha\":\"f36df489556ab499d4d0b3e8e22584f05c98f02d\",");
        jsonBuilder.append("\"author\":{");
        jsonBuilder.append("\"email\":\"arhrodriguez@gmail.com\",");
        jsonBuilder.append("\"name\":\"Ariel Horacio Rodriguez\"");
        jsonBuilder.append("},");
        jsonBuilder.append("\"message\":\"Automatic update\",");
        jsonBuilder.append("\"distinct\":true,");
        jsonBuilder.append("\"url\":\"https://api.github.com/repos/arrodriguez/losiunicos/commits/f36df489556ab499d4d0b3e8e22584f05c98f02d\"");
        jsonBuilder.append("}]");  // Close the commits array
        jsonBuilder.append("}");   // Close the payload object
        jsonBuilder.append("}");  // Close event object
        jsonBuilder.append("]");  // Close the main array

        return jsonBuilder.toString();
    }

    public static String mockCommandOutput(String versionId, Boolean updated, Boolean prettyPrint) throws JsonProcessingException {
        VersionID versionID = new VersionID(versionId);
        versionID.setUpdated(updated);

        ObjectMapper mapper = new ObjectMapper();

        if (Boolean.TRUE.equals(prettyPrint)) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        return mapper.writeValueAsString(versionID);
    }
}
