package com.kenzie.capstone.service.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kenzie.capstone.service.StudySessionService;
import com.kenzie.capstone.service.converter.JsonStringToArrayListStringsConverter;
import com.kenzie.capstone.service.converter.JsonStringToStudySessionConverter;
import com.kenzie.capstone.service.dependency.DaggerServiceComponent;
import com.kenzie.capstone.service.dependency.ServiceComponent;
import com.kenzie.capstone.service.exceptions.InvalidDataException;
import com.kenzie.capstone.service.model.StudySession;
import com.kenzie.capstone.service.model.StudySessionRequest;
import com.kenzie.capstone.service.model.StudySessionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class DeleteStudySession implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    static final Logger log = LogManager.getLogger();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        JsonStringToArrayListStringsConverter jsonStringToArrayListStringsConverter = new JsonStringToArrayListStringsConverter();
        JsonStringToStudySessionConverter jsonStringToStudySessionConverter = new JsonStringToStudySessionConverter();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        // Logging the request json to make debugging easier.
        log.info(gson.toJson(input));

        ServiceComponent serviceComponent = DaggerServiceComponent.create();
        StudySessionService studySessionService = serviceComponent.provideStudySessionService();

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        //TODO change to single string instead of List... want to do multiple? Or just keep it individual?
        try {
//            List<String> sessionId = jsonStringToArrayListStringsConverter.convert(input.getBody());
            String sessionId = input.getPathParameters().get("sessionId");

//            StudySessionRequest studySessionRequest = jsonStringToStudySessionConverter.convert(input.getBody());
//            StudySessionResponse studySessionResponse = studySessionService.getStudySessionBySessionId(studySessionRequest.)


            boolean allDeleted = studySessionService.deleteStudySession(sessionId);

            return response
                    .withStatusCode(200)
                    .withBody(gson.toJson(allDeleted));
        } catch(InvalidDataException e){
            return response
                    .withStatusCode(400)
                    .withBody(gson.toJson(e.errorPayload()));
        }
    }
}
