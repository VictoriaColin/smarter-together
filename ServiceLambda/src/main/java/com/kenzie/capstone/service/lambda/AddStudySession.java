package com.kenzie.capstone.service.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.kenzie.capstone.service.StudySessionService;
import com.kenzie.capstone.service.converter.JsonStringToStudySessionConverter;
import com.kenzie.capstone.service.dependency.DaggerServiceComponent;
import com.kenzie.capstone.service.dependency.ServiceComponent;
import com.kenzie.capstone.service.exceptions.InvalidDataException;

import com.kenzie.capstone.service.model.StudySessionRequest;
import com.kenzie.capstone.service.model.StudySessionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AddStudySession implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    static final Logger log = LogManager.getLogger();

    @Override
//    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
//        JsonStringToStudySessionConverter jsonStringToStudySessionConverter = new JsonStringToStudySessionConverter();
//        GsonBuilder builder = new GsonBuilder();
//        Gson gson = builder.create();
//
//        log.info(gson.toJson(input));
//
//        ServiceComponent serviceComponent = DaggerServiceComponent.create();
//        StudySessionService studySessionService = serviceComponent.provideStudySessionService();
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//
//        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
//                .withHeaders(headers);
//
//        String data = input.getBody();
//
//        if (data == null || data.length() == 0) {
//            return response
//                    .withStatusCode(400)
//                    .withBody("data is invalid");
//        }
//
//        try {
//            StudySessionRequest studySessionRequest = jsonStringToStudySessionConverter.convert(input.getBody());
//            StudySessionResponse studySessionResponse = studySessionService.addStudySession(studySessionRequest);
//            return response
//                    .withStatusCode(200)
//                    .withBody(gson.toJson(studySessionResponse));
//        } catch (InvalidDataException e) {
//            return response
//                    .withStatusCode(400)
//                    .withBody(gson.toJson(e.errorPayload()));
//        }
//    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        JsonStringToStudySessionConverter jsonStringToStudySessionConverter = new JsonStringToStudySessionConverter();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        // Logging the request json to make debugging easier.
        log.info(gson.toJson(input));

        ServiceComponent serviceComponent = DaggerServiceComponent.create();
        StudySessionService studySessionService = serviceComponent.provideStudySessionService();

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            StudySessionRequest studySessionRequest = jsonStringToStudySessionConverter.convert(input.getBody());
            StudySessionResponse studySessionResponse = studySessionService.addStudySession(studySessionRequest);
            return response
                    .withStatusCode(200)
                    .withBody(gson.toJson(studySessionResponse));
        } catch (InvalidDataException e) {
            return response
                    .withStatusCode(400)
                    .withBody(gson.toJson(e.errorPayload()));
        }
    }
}
