package org.onosproject.sampleapp.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.config.Config;
import org.onosproject.sampleapp.data.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AConfiguration extends Config<ApplicationId> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public A getA() {
        JsonNode jsonA = object.path("a");
        return new A(jsonA);
    }
}
