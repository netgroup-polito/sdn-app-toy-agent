/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.frogsssa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * REST Web Service
 *
 * @author lara
 */
@Path("{AppId}")
public class serviceLayerService {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of serviceLayerService
     */
    public serviceLayerService() {
    }

    /*
    private static final String JSON_DEEP_0 = "25";
    private static final String JSON_DEEP_1 = "{\"l1\":25,\"l2\":\"Applic1\"}";
    private static final String JSON_DEEP_2 = "{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"}";
    private static final String JSON_DEEP_3 = "[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":{}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]";
    private static final String JSON_DEEP_4 = "{\"c\":{\"l1\":66,\"l2\":\"json\",\"l3\":\"js\"},\"l1\":50," +
            "\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":{}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]}";
    private static final String JSON_DEEP_5 = "{\"b\":{\"c\":{\"l1\":66,\"l2\":\"json\",\"l3\":\"js\"},\"l1\":50," +
            "\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":{}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\"," +
            "\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}";
    private static final String JSON_DEEP_6 = "{\"a\":{\"b\":{\"c\":{\"l1\":66,\"l2\":\"json\",\"l3\":\"js\"}," +
            "\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":77,\"l2\":\"list1\"},{\"c\":{\"a\":{}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":1,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\"," +
            "\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}";
    private static final String JSON_DEEP_7 = "{\"b\":{\"c\":{},\"l1\":50,\"l2\":\"test\",\"l\":[]}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":66,\"l2\":\"json\",\"l3\":\"js\"},\"l1\":50,\"l2\":\"foll2\"," +
            "\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77," +
            "\"l2\":\"list1\"},{\"c\":{\"a\":{}},\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]}," +
            "\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}";
    private static final String JSON_DEEP_8 = "{\"a\":{\"b\":{\"c\":{},\"l1\":50,\"l2\":\"test\",\"l\":[]}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":66,\"l2\":\"json\",\"l3\":\"js\"},\"l1\":50,\"l2\":\"foll2\"," +
            "\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":77," +
            "\"l2\":\"list1\"},{\"c\":{\"a\":{}},\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}]}," +
            "\"c\":{\"a\":{\"l1\":44,\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"}}";
    */

    private static final String JSON_DEEP_0 = "96";
    private static final String JSON_DEEP_1 = "{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"}";
    private static final String JSON_DEEP_2 = "{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"}," +
            "{\"l1\":46,\"l2\":\"lista1\"}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"}}";
    private static final String JSON_DEEP_3 = "[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":1,\"l2\":\"list1\",\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45," +
            "\"l2\":\"lista111\"},{\"l1\":46,\"l2\":\"lista1\"}],\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"}}}," +
            "{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"}},\"l1\":11,\"l2\":\"liss\"},{\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":77,\"l2\":\"Str3\"}]";
    private static final String JSON_DEEP_4 = "{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]}," +
            "\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58," +
            "\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":1,\"l2\":\"list1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":45,\"l2\":\"lista111\"},{\"l1\":46,\"l2\":\"lista1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"}}},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":77,\"l2\":\"Str3\"}]}";
    private static final String JSON_DEEP_5 = "{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]}," +
            "\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58," +
            "\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":1,\"l2\":\"list1\",\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[{\"l1\":45,\"l2\":\"lista111\"},{\"l1\":46,\"l2\":\"lista1\"}],\"c\":{\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"}}},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":77,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"}," +
            "\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]},\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"l1\":30," +
            "\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}";
    private static final String JSON_DEEP_6 = "{\"a\":{\"b\":{\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\"," +
            "\"l\":[]},\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":67,\"l2\":\"tor\"},\"l1\":58," +
            "\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\",\"l3\":\"last\"},\"l1\":1,\"l2\":\"list1\",\"b\":{\"l1\":54," +
            "\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"},{\"l1\":46,\"l2\":\"lista1\"}],\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"}}},{\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"}},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"}," +
            "\"l1\":77,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"}," +
            "\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]},\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"l1\":30," +
            "\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44," +
            "\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44,\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":20," +
            "\"l2\":\"test4\",\"l3\":\"orchestrator\"}";
    private static final String JSON_DEEP_7 = "{\"b\":{\"c\":{\"c\":{\"a\":{\"b\":{\"c\":{},\"l\":[]}," +
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
            "\"l1\":50,\"l2\":\"foll2\",\"l\":[{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25," +
            "\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":3,\"l2\":\"Str3\"}," +
            "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{}},\"l1\":0,\"l2\":\"Applyyyyy\"},{\"b\":{\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":45,\"l2\":\"lista111\"}," +
            "{\"l1\":46,\"l2\":\"lista1\"}]},\"c\":{\"a\":{\"l1\":67,\"l2\":\"tor\"},\"l1\":58,\"l2\":\"foglia\"," +
            "\"l3\":\"last\"},\"l1\":1,\"l2\":\"list1\"},{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":67," +
            "\"l2\":\"tor\"},\"l1\":0,\"l2\":\"lara\",\"l3\":\"orchesrator\"},\"l1\":11,\"l2\":\"liss\"}," +
            "{\"b\":{\"c\":{},\"l\":[]},\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":20,\"l2\":\"test4\"," +
            "\"l3\":\"orchestrator\"},\"l1\":77,\"l2\":\"Str3\"}]},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44," +
            "\"l2\":\"foll1\"},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":25,\"l2\":\"Applic1\"}," +
            "\"c\":{\"a\":{\"b\":{\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\"," +
            "\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"b\":{\"l1\":54,\"l2\":\"final\",\"l\":[]},\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96," +
            "\"l3\":\"lista3\",\"l2\":\"96\"},\"l1\":44,\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"c\":{\"l1\":96," +
            "\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":54,\"l2\":\"final\",\"l\":[{\"l1\":25,\"l2\":\"Applic1\"}]}," +
            "\"c\":{\"a\":{\"l1\":25,\"l2\":\"Applic1\"},\"l1\":96,\"l2\":\"96\",\"l3\":\"lista3\"},\"l1\":44," +
            "\"l2\":\"foll1\"},\"c\":{\"a\":{\"b\":{\"l\":[]},\"c\":{}}},\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"}," +
            "\"l1\":30,\"l2\":\"req\",\"l3\":\"usr\"},\"l1\":20,\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1," +
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
            "\"l2\":\"test4\",\"l3\":\"orchestrator\"},\"l1\":1,\"l2\":\"Str3\"},\"switches\":[{\"id\":\"1\"," +
            "\"ip\":\"10.0.0.1\",\"mac\":\"00:00:00:00:00:11\"},{\"id\":\"2\",\"ip\":\"10.0.0.2\"," +
            "\"mac\":\"00:00:00:00:00:22\"},{\"id\":\"3\",\"ip\":\"10.0.0.3\",\"mac\":\"00:00:00:00:00:33\"}," +
            "{\"id\":\"4\",\"ip\":\"10.0.0.4\",\"mac\":\"00:00:00:00:00:44\"},{\"id\":\"5\",\"ip\":\"10.0.0.5\"," +
            "\"mac\":\"00:00:00:00:00:55\"},{\"id\":\"6\",\"ip\":\"10.0.0.6\",\"mac\":\"00:00:00:00:00:66\"}," +
            "{\"id\":\"7\",\"ip\":\"10.0.0.7\",\"mac\":\"00:00:00:00:00:77\"}],\"load\":[{\"id\":\"1\",\"tp\":823.0}," +
            "{\"id\":\"2\",\"tp\":655.0},{\"id\":\"3\",\"tp\":830.0},{\"id\":\"4\",\"tp\":634.0},{\"id\":\"5\"," +
            "\"tp\":533.0},{\"id\":\"6\",\"tp\":666.0},{\"id\":\"7\",\"tp\":802.0}]}";

    //get the resources
    @Path("{varId : .+}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResources(@PathParam("AppId") String id, @PathParam("varId") String var){
        System.out.println("GET REQUEST - id:'"+id+"' - var:'"+var+"'");
        //String res = new String("asked for this variable " + var);
        //Object obj = ConnectionModule.getResVariable(id, var);
        //if(obj!=null){
          //  return (new Gson()).toJson(obj);
        //}
        //var = var.replace("/", ".");
        JsonNode obj = ((new ConnectionModule()).getValue(id, var));
        System.out.println(obj);
        if(obj==null)
            return Response.status(Response.Status.NOT_FOUND).build();
        if(obj.asText().equals("null"))
            return Response.status(Response.Status.PARTIAL_CONTENT).build();
        return Response.ok(obj.toString()).build();
    }
    
    @GET
    @Path("DM")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getYang(@PathParam("AppId") String id){
        System.err.println("Requested Data Model for app:" + id);
        String yang = ConnectionModule.getYang(id);
        if(yang==null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(yang).build();
    }
    
    //configuration
    @Path("{varId: .+}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setConf(@PathParam("AppId") String id, @PathParam("varId") String var, String Json){
        //0 ok
        //1 variable not setted - error
        //2 variable not found - error
        //3 internal server error
        //4 app not found
        //controllo validità variabile
        //ConnectionModule.someConfiguration(id.toString(), "config " + var + " " + Json);
        System.out.println("POST REQUEST - id:'"+id+"' - var:'"+var+"'");
        //var = var.replace("/", ".");
        JsonNode node = ConnectionModule.configVar(id, var, Json);
        Integer configured = node.get("res").asInt(5);
        Response res;
        switch(configured){
            case 0:
                res = Response.ok(node.toString()).build();
                break;
            case 1:
                res = Response.status(Response.Status.BAD_REQUEST).build();
                break;
            case 2:
                res = Response.status(Response.Status.NOT_FOUND).build();
                break;
            case 3:
                res = Response.serverError().build();
                break;
            case 4:
                res = Response.status(Response.Status.NOT_FOUND).build();
                break;
            default:
                res = Response.serverError().build();
        }
        return res;
    }
    
    @Path("{varId: .+}")
    @DELETE
    public Response deleteVar(@PathParam("AppId") String id, @PathParam("varId") String var){
        //var = var.replace("/", ".");
        Integer deleted = ConnectionModule.deleteVar(id, var);
                Response res;
        switch(deleted){
            case 0:
                res = Response.noContent().build();
                break;
            case 1:
                res = Response.status(Response.Status.BAD_REQUEST).build();
                break;
            case 2:
                res = Response.status(Response.Status.NOT_FOUND).build();
                break;
            case 3:
                res = Response.serverError().build();
                break;
            case 4:
                res = Response.status(Response.Status.NOT_FOUND).build();
                break;
            default:
                res = Response.serverError().build();
        }
        return res;
    }

    public static double findSumWithoutUsingStream(double[] array) {
        double sum = 0;
        for (double value : array) {
            sum += value;
        }
        return sum;
    }

    @GET
    @Path("/readtest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response readTest() {

        int testRepetition = 1000;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode avgTimes = new ObjectNode(JsonNodeFactory.instance);
        double[] times = new double[testRepetition];

        try {

            System.out.println("Read test for deep 0...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a/c/a/b/l[1]/b/c/l1");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("0", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 0: " + avgTimes.get("0"));

            System.out.println("Read test for deep 1...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a/c/a/b/l[1]/b/c");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("1", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 1: " + avgTimes.get("1"));

            System.out.println("Read test for deep 2...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a/c/a/b/l[1]/b");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("2", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 2: " + avgTimes.get("2"));

            System.out.println("Read test for deep 3...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a/c/a/b/l");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("3", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 3: " + avgTimes.get("3"));

            System.out.println("Read test for deep 4...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a/c/a/b");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("4", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 4: " + avgTimes.get("4"));

            System.out.println("Read test for deep 5...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a/c/a");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("5", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 5: " + avgTimes.get("5"));

            System.out.println("Read test for deep 6...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a/c");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("6", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 6: " + avgTimes.get("6"));

            System.out.println("Read test for deep 7...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator/a");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("7", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 7: " + avgTimes.get("7"));

            System.out.println("Read test for deep 8...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = getResources("sssaLara", "orchestrator");
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("8", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 8: " + avgTimes.get("8"));

        } catch (InterruptedException | IOException ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.ok(avgTimes.toString()).build();
    }

    @GET
    @Path("/writetest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeTest() {

        int testRepetition = 1000;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode avgTimes = new ObjectNode(JsonNodeFactory.instance);
        double[] times = new double[testRepetition];

        try {

            System.out.println("Write test for deep 0...");
            avgTimes.put("0", findSumWithoutUsingStream(times)/times.length);
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                Response response = setConf("sssaLara", "orchestrator/a/c/a/b/l[1]/b/c/l1", JSON_DEEP_0);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("0", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 0: " + avgTimes.get("0"));

            System.out.println("Write test for deep 1...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                Response response = setConf("sssaLara", "orchestrator/a/c/a/b/l[1]/b/c", JSON_DEEP_1);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("1", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 1: " + avgTimes.get("1"));

            System.out.println("Write test for deep 2...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = setConf("sssaLara", "orchestrator/a/c/a/b/l[1]/b", JSON_DEEP_2);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("2", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 2: " + avgTimes.get("2"));

            System.out.println("Write test for deep 3...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = setConf("sssaLara", "orchestrator/a/c/a/b/l", JSON_DEEP_3);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("3", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 3: " + avgTimes.get("3"));

            System.out.println("Write test for deep 4...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = setConf("sssaLara", "orchestrator/a/c/a/b", JSON_DEEP_4);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("4", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 4: " + avgTimes.get("4"));

            System.out.println("Write test for deep 5...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = setConf("sssaLara", "orchestrator/a/c/a", JSON_DEEP_5);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("5", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 5: " + avgTimes.get("5"));

            System.out.println("Write test for deep 6...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = setConf("sssaLara", "orchestrator/a/c", JSON_DEEP_6);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("6", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 6: " + avgTimes.get("6"));

            System.out.println("Write test for deep 7...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = setConf("sssaLara", "orchestrator/a", JSON_DEEP_7);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("7", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 7: " + avgTimes.get("7"));

            System.out.println("Write test for deep 8...");
            for (int i = 0; i < testRepetition; i++) {
                Thread.sleep(1);
                long begin = System.nanoTime();
                Response response = setConf("sssaLara", "orchestrator", JSON_DEEP_8);
                if (response.getStatus() != 200) {
                    return response;
                }
                times[i] = mapper.readTree((String) response.getEntity()).get("time").asDouble();
                System.out.println("time: " + times[i]);
                Thread.sleep(1);
            }
            avgTimes.put("8", findSumWithoutUsingStream(times)/times.length);
            System.out.println("deep 8: " + avgTimes.get("8"));

        } catch (InterruptedException | IOException ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        return Response.ok(avgTimes.toString()).build();

    }
}
