/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.sampleapp.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.sampleapp.AppComponentService;
import org.onosproject.sampleapp.data.A;
import org.onosproject.sampleapp.data.B;
import org.onosproject.sampleapp.data.C;
import org.onosproject.sampleapp.data.Load;
import org.onosproject.sampleapp.data.Message;
import org.onosproject.sampleapp.data.Switch;
import org.slf4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * REST APIs for create/delete a bridge and create a port.
 */

@Path("/")
public class ConfigWebResource extends AbstractWebResource {

    private final Logger log = getLogger(getClass());

    private static final String JSON_DEEP_0 = "96";
    private static final String JSON_DEEP_1 = "{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}";
    private static final String JSON_DEEP_2 = "{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"," +
            "\"b\":null,\"c\":null},{\"l1\":46,\"l2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}";
    private static final String JSON_DEEP_3 = "[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null},\"l1\":58," +
            "\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":45,\"l2\":\"lista111\",\"b\":null,\"c\":null},{\"l1\":46,\"l2\":\"lista1\",\"b\":null," +
            "\"c\":null}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}},{\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\",\"b\":null}},\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"}," +
            "\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]";
    private static final String JSON_DEEP_4 = "{\"c\":{\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\",\"a\":{\"l1\":67," +
            "\"l2\":\"tor\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}}},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\",\"b\":null},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"," +
            "\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\",\"b\":null,\"c\":null}," +
            "{\"l1\":46,\"l2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"," +
            "\"a\":null}}},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]}";
    private static final String JSON_DEEP_5 = "{\"b\":{\"c\":{\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"," +
            "\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}},\"l1\":50,\"l2\":\"foll2\"," +
            "\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"}," +
            "\"l1\":77,\"l2\":\"list1\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"," +
            "\"b\":null,\"c\":null},{\"l1\":46,\"l2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null}}," +
            "\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"," +
            "\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\",\"b\":null}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}";
    private static final String JSON_DEEP_6 = "{\"a\":{\"b\":{\"c\":{\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"," +
            "\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}},\"l1\":50,\"l2\":\"foll2\"," +
            "\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"}," +
            "\"l1\":77,\"l2\":\"list1\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"," +
            "\"b\":null,\"c\":null},{\"l1\":46,\"l2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null}}," +
            "\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"," +
            "\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\",\"b\":null}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}," +
            "\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\",\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"," +
            "\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":{\"l1\":25,\"l2\":\"Applic1\",\"b\":null}}},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":{\"l1\":25,\"l2\":\"Applic1\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]," +
            "\"c\":null}}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\",\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"," +
            "\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\",\"b\":null}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\",\"c\":{\"a\":{\"l1\":44," +
            "\"l2\":\"foll1\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\",\"c\":null}}}}";
    private static final String JSON_DEEP_7 = "{\"b\":{\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"," +
            "\"b\":null,\"c\":null},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\"," +
            "\"l\":[{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"a\":null,\"l1\":58," +
            "\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":null,\"c\":null,\"l1\":1}," +
            "\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":null,\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[],\"c\":null},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}},\"l1\":30," +
            "\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\",\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[],\"c\":{\"a\":null,\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}},\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25,\"l2\":\"Applic1\",\"b\":null}}},\"l1\":30," +
            "\"l2\":\"req\",\"l3\":\"usr\",\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":{\"l1\":54," +
            "\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\",\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":null,\"c\":null}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\",\"c\":{\"a\":null,\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"}}}}},\"l1\":50,\"l2\":\"test\",\"l\":[{\"b\":{\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\"," +
            "\"l\":[{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\",\"b\":null,\"c\":null}," +
            "{\"l1\":46,\"l2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"," +
            "\"a\":null}},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null}}," +
            "\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"," +
            "\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\",\"b\":null}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}," +
            "{\"b\":{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null}," +
            "\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45," +
            "\"l2\":\"lista111\",\"b\":null,\"c\":null},{\"l1\":46,\"l2\":\"lista1\",\"b\":null,\"c\":null}]," +
            "\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"," +
            "\"b\":null},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"}," +
            "{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}," +
            "\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25,\"l2\":\"Applic1\",\"b\":null}}}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\",\"a\":{\"l1\":67,\"l2\":\"tor\"," +
            "\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"," +
            "\"a\":null}}},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null}," +
            "\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\",\"b\":{\"l1\":54," +
            "\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\",\"b\":null,\"c\":null},{\"l1\":46," +
            "\"l2\":\"lista1\",\"b\":null,\"c\":null}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"," +
            "\"a\":null}}},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\",\"b\":null}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":null}}," +
            "\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25,\"l2\":\"Applic1\",\"b\":null}}}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\",\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"," +
            "\"a\":{\"l1\":25,\"l2\":\"Applic1\",\"b\":null}}},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"," +
            "\"a\":{\"l1\":25,\"l2\":\"Applic1\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null}}}},\"l1\":30," +
            "\"l2\":\"req\",\"l3\":\"usr\",\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\",\"b\":{\"l1\":54," +
            "\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\",\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\",\"b\":null}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\",\"c\":{\"a\":{\"l1\":44," +
            "\"l2\":\"foll1\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[],\"c\":null},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\",\"a\":null}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\",\"c\":null}}}},\"l1\":1," +
            "\"l2\":\"Str3\"}";
    private static final String JSON_DEEP_8 = "{\"a\":{\"b\":{\"c\":{\"c\":{\"a\":{\"b\":{\"c\":{},\"l\":[]}," +
            "\"c\":{\"a\":{}}}},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":50,\"l2\":\"test\"," +
            "\"l\":[{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50," +
            "\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54," +
            "\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}," +
            "{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50," +
            "\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54," +
            "\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"}," +
            "\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54," +
            "\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"},{\"l1\":46,\"l2\":\"lista1\"}]}," +
            "\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":1," +
            "\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":0," +
            "\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"},{\"b\":{\"c\":{},\"l\":[]}," +
            "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":3,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"}," +
            "\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]},\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"l1\":30," +
            "\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96," +
            "\"l3\":\"96\",\"l2\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44," +
            "\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"l\":[]},\"c\":{}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1," +
            "\"l2\":\"Str3\"},\"a2\":{\"b\":{\"c\":{\"c\":{\"a\":{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{}}}}," +
            "\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":50,\"l2\":\"test\"," +
            "\"l\":[{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50," +
            "\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54," +
            "\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}," +
            "{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50," +
            "\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54," +
            "\"l2\":\"final\",\"l\":[]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]},\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"}," +
            "\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":0," +
            "\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"}," +
            "\"l1\":25,\"l2\":\"Applic1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"}," +
            "\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25," +
            "\"l2\":\"Applic1\"}]},\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"l\":[]},\"c\":{}}}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":20," +
            "\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"},\"switches\":[{\"id\":1," +
            "\"ip\":\"10.0.0.1\",\"mac\":\"00:00:00:00:00:11\"},{\"id\":2,\"ip\":\"10.0.0.2\"," +
            "\"mac\":\"00:00:00:00:00:22\"},{\"id\":3,\"ip\":\"10.0.0.3\",\"mac\":\"00:00:00:00:00:33\"},{\"id\":4," +
            "\"ip\":\"10.0.0.4\",\"mac\":\"00:00:00:00:00:44\"},{\"id\":5,\"ip\":\"10.0.0.5\"," +
            "\"mac\":\"00:00:00:00:00:55\"},{\"id\":6,\"ip\":\"10.0.0.6\",\"mac\":\"00:00:00:00:00:66\"},{\"id\":7," +
            "\"ip\":\"10.0.0.7\",\"mac\":\"00:00:00:00:00:77\"}],\"load\":[{\"id\":\"1\",\"tp\":823.0},{\"id\":\"2\"," +
            "\"tp\":655.0},{\"id\":\"3\",\"tp\":830.0},{\"id\":\"4\",\"tp\":634.0},{\"id\":\"5\",\"tp\":533.0}," +
            "{\"id\":\"6\",\"tp\":666.0},{\"id\":\"7\",\"tp\":802.0}]}";


    @GET
    @Path("/test")
    public Response getTest() {
        ObjectNode responseBody = new ObjectNode(JsonNodeFactory.instance);
        responseBody.put("message", "it works!");
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/init")
    public Response init() {
        AppComponentService appComponent = get(AppComponentService.class);
        appComponent.init();
        return Response.status(200).build();
    }

    @POST
    @Path("/initfull")
    public Response initFull() {
        AppComponentService appComponent = get(AppComponentService.class);
        appComponent.initializeFull();
        return Response.status(200).build();
    }

    @GET
    @Path("/repetition")
    public Response getRepetition() {
        AppComponentService appComponent = get(AppComponentService.class);
        return Response.status(200).entity(appComponent.getTestRepetition()).build();
    }

    @POST
    @Path("/repetition")
    public Response setRepetition(InputStream body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.setTestRepetition(mapper.readValue(body, Integer.class));
            return Response.status(200).build();
        } catch (IOException ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("/acwtest")
    public Response acvTest() {

        AppComponentService appComponent = get(AppComponentService.class);
        appComponent.init();
        int testRepetition = appComponent.getTestRepetition();
        ObjectNode avgTimes = new ObjectNode(JsonNodeFactory.instance);
        double[] times = new double[testRepetition];

        try {
            log.info("Write test for deep 6...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCc(new ByteArrayInputStream(JSON_DEEP_6.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("6", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 6: " + avgTimes.get("6"));

            log.info("Write test for deep 6 without mapper...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCcNoMapper(new ByteArrayInputStream(JSON_DEEP_6.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("6_t", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 6 (no mapper): " + avgTimes.get("6_t"));

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.status(200).entity(avgTimes).build();

    }

    @GET
    @Path("/readtest")
    public Response readTest() {

        AppComponentService appComponent = get(AppComponentService.class);
        appComponent.initializeFull();
        int testRepetition = appComponent.getTestRepetition();
        ObjectNode avgTimes = new ObjectNode(JsonNodeFactory.instance);
        double[] times = new double[testRepetition];

        try {

            log.info("Read test for deep 0...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAaCcAaBbLlBbCcL1(0);
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("0", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 0: " + avgTimes.get("0"));

            log.info("Read test for deep 1...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAaCcAaBbLlBbCc(0);
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("1", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 1: " + avgTimes.get("1"));

            log.info("Read test for deep 2...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAaCcAaBbLlBb(0);
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("2", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 2: " + avgTimes.get("2"));

            log.info("Read test for deep 3...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAaCcAaBbLl();
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("3", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 3: " + avgTimes.get("3"));

            log.info("Read test for deep 4...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAaCcAaBb();
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("4", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 4: " + avgTimes.get("4"));

            log.info("Read test for deep 5...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAaCcAa();
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("5", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 5: " + avgTimes.get("5"));

            log.info("Read test for deep 6...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAaCc();
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("6", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 6: " + avgTimes.get("6"));

            log.info("Read test for deep 7...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getAa();
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("7", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 7: " + avgTimes.get("7"));

            log.info("Read test for deep 8...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = getX();
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.getEntity().toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("8", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 8: " + avgTimes.get("8"));

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.status(200).entity(avgTimes).build();
    }

    @GET
    @Path("/writetest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeTest() {

        AppComponentService appComponent = get(AppComponentService.class);
        appComponent.init();
        int testRepetition = appComponent.getTestRepetition();
        ObjectNode avgTimes = new ObjectNode(JsonNodeFactory.instance);
        double[] times = new double[testRepetition];

        try {

            log.info("Write test for deep 0...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCcAaBbLlBbCcL1(new ByteArrayInputStream(JSON_DEEP_0.getBytes()), 0);
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("0", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 0: " + avgTimes.get("0"));

            log.info("Write test for deep 1...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCcAaBbLlBbCc(new ByteArrayInputStream(JSON_DEEP_1.getBytes()), 0);
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("1", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 1: " + avgTimes.get("1"));

            log.info("Write test for deep 2...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCcAaBbLlBb(new ByteArrayInputStream(JSON_DEEP_2.getBytes()), 0);
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("2", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 2: " + avgTimes.get("2"));

            log.info("Write test for deep 3...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCcAaBbLl(new ByteArrayInputStream(JSON_DEEP_3.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("3", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 3: " + avgTimes.get("3"));

            log.info("Write test for deep 4...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCcAaBb(new ByteArrayInputStream(JSON_DEEP_4.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("4", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 4: " + avgTimes.get("4"));

            log.info("Write test for deep 5...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCcAa(new ByteArrayInputStream(JSON_DEEP_5.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("5", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 5: " + avgTimes.get("5"));

            log.info("Write test for deep 6...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAaCc(new ByteArrayInputStream(JSON_DEEP_6.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("6", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 6: " + avgTimes.get("6"));

            log.info("Write test for deep 7...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setAa(new ByteArrayInputStream(JSON_DEEP_7.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("7", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 7: " + avgTimes.get("7"));

            log.info("Write test for deep 8...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                double begin = System.nanoTime();
                Response response = setX(new ByteArrayInputStream(JSON_DEEP_8.getBytes()));
                if (response.getStatus() != 200) {
                    return response;
                }
                log.debug(response.toString());
                double end = System.nanoTime();
                times[i] = (double) (end - begin) / 1000000;
                log.info("time: " + times[i]);
                appComponent.init();
                Thread.sleep(1);
            }
            avgTimes.put("8", Arrays.stream(times).average().orElse(Double.NaN));
            log.info("deep 8: " + avgTimes.get("8"));

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.status(200).entity(avgTimes).build();

    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getX() {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        ObjectNode responseBody = new ObjectNode(JsonNodeFactory.instance);
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody.set("a", appComponent.getA().toJson());
        responseBody.set("a2", appComponent.getA2().toJson());
        responseBody.putArray("load");
        for (Load l : appComponent.getLoadList()) {
            ((ArrayNode) responseBody.get("load")).add(l != null ? l.toJson() : null);
        }
        responseBody.putArray("switches");
        for (Switch s : appComponent.getSwitches()) {
            ((ArrayNode) responseBody.get("switches")).add(s != null ? s.toJson() : null);
        }
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setX(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode requestBody = mapper.readTree(body);
            A a = new A(mapper.writeValueAsString(requestBody.get("a")));
            log.info("hello");
            A a2 = new A(mapper.writeValueAsString(requestBody.get("a2")));
            log.info("hello2");
            List<Switch> switches = new ArrayList<>();
            for (JsonNode n : requestBody.get("switches")) {
                log.info("hello3");
                Switch s = new Switch(n);
                switches.add(s);
            }
            log.info("hello4");
            List<Load> loads = new ArrayList<>();
            for (JsonNode n : requestBody.get("load")) {
                log.info("hello5");
                Load l = new Load(n);
                loads.add(l);
            }
            log.info("hello6");
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.setA(a);
            appComponent.setA2(a2);
            appComponent.setSwitchList(switches);
            appComponent.setLoadList(loads);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAa() {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAa(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            A a = new A(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.setA(a);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCc() {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().toJson();
        //ObjectMapper mapper = new ObjectMapper();
        //responseBody = mapper.convertValue(appComponent.getA().getC(), C.class);
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c-test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcNoMapper(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().setC(new C(mapper.writeValueAsString(jsonNode)));
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @POST
    @Path("/a/c")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCc(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            C c = new C(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().setC(c);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAa() {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAa(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            A a = new A(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().setA(a);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBb() {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBb(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            B b = new B(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().setB(b);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/c")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBbCc() {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().getC().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/c")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbCc(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            C c = new C(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().setC(c);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBbLl() {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            AppComponentService appComponent = get(AppComponentService.class);
            List<A> aList = appComponent.getA().getC().getA().getB().getL();
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode jsonList = mapper.createArrayNode();
            for (A a : aList) {
                jsonList.add(a.toJson());
            }
            String responseBody = mapper.writeValueAsString(jsonList);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).entity(responseBody).build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unable to serialize list").build();
        }

    }

    @POST
    @Path("/a/c/a/b/l")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLl(InputStream body) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            List<A> aList = new ArrayList<A>();
            for (JsonNode n : mapper.readTree(body)) {
                A a = new A(mapper.writeValueAsString(n));
                aList.add(a);
            }
            //List<A> aList = mapper.readValue(body, new TypeReference<List<A>>() { });
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().setL(aList);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l/{index}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBbLl(@PathParam("index") int index) {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().getL().get(index).toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/l/{index}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLl(InputStream body, @PathParam("index") int index) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            A a = new A(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().getL().set(index, a);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l/{index}/c")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBbLlCc(@PathParam("index") int index) {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().getL().get(index).getC().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/l/{index}/c")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLlCc(InputStream body, @PathParam("index") int index) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            C c = new C(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().getL().get(index).setC(c);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l/{index}/b")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBbLlBb(@PathParam("index") int index) {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().getL().get(index).getB().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/l/{index}/b")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLlBb(InputStream body, @PathParam("index") int index) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            B b = new B(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().getL().get(index).setB(b);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l/{index}/c/a")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBbLlCcAa(@PathParam("index") int index) {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().getL().get(index).getC().getA().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/l/{index}/c/a")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLlCcAa(InputStream body, @PathParam("index") int index) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            //A a = mapper.readValue(body, A.class);
            A a = new A(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().getL().get(index).getC().setA(a);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l/{index}/b/c")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAaCcAaBbLlBbCc(@PathParam("index") int index) {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        JsonNode responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().getL().get(index).getB().getC().toJson();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/l/{index}/b/c")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLlBbCc(InputStream body, @PathParam("index") int index) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            C c = new C(mapper.writeValueAsString(mapper.readTree(body)));
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().getL().get(index).getB().setC(c);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l/{index}/b/c/l1")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAaCcAaBbLlBbCcL1(@PathParam("index") int index) {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        int responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        A a = appComponent.getA().getC().getA().getB().getL().get(index);
        B b = a.getB();
        C c = b.getC();
        responseBody = c.getL1();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/l/{index}/b/c/l1")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLlBbCcL1(InputStream body, @PathParam("index") int index) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            int l1 = mapper.readValue(body, Integer.class);
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().getL().get(index).getB().getC().setL1(l1);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/a/c/a/b/l/{index}/c/a/l1")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAaCcAaBbLlCcAaL1(@PathParam("index") int index) {
        double begin = System.nanoTime();
        log.debug("begin: " + begin);
        int responseBody;
        AppComponentService appComponent = get(AppComponentService.class);
        responseBody = appComponent.getA().getC().getA().getB().getL().get(index).getC().getA().getL1();
        double end = System.nanoTime();
        log.debug("end: " + begin);
        log.debug("time: " + ((double) (end - begin) / 1000000));
        return Response.status(200).entity(responseBody).build();
    }

    @POST
    @Path("/a/c/a/b/l/{index}/c/a/l1")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAaCcAaBbLlCcAaL1(InputStream body, @PathParam("index") int index) {
        try {
            double begin = System.nanoTime();
            log.debug("begin: " + begin);
            ObjectMapper mapper = new ObjectMapper();
            int l1 = mapper.readValue(body, Integer.class);
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.getA().getC().getA().getB().getL().get(index).getC().getA().setL1(l1);
            double end = System.nanoTime();
            log.debug("end: " + begin);
            log.debug("time: " + ((double) (end - begin) / 1000000));
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }

    @GET
    @Path("/message")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessage() {
        try {
            AppComponentService appComponent = get(AppComponentService.class);
            ObjectMapper mapper = new ObjectMapper();
            String responseBody = mapper.writeValueAsString(appComponent.getMessage());
            return Response.status(200).entity(responseBody).build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Serialization Error").build();
        }
    }

    @POST
    @Path("/message")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setMessage(InputStream body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Message message = mapper.readValue(body, Message.class);
            AppComponentService appComponent = get(AppComponentService.class);
            appComponent.setMessage(message);
            return Response.status(200).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Unable to parse request body").build();
        }
    }
}
