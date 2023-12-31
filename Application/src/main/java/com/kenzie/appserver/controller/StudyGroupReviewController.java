package com.kenzie.appserver.controller;

import com.kenzie.appserver.controller.model.GroupReviewResponse;
import com.kenzie.appserver.controller.model.StudyGroupReviewRequest;
import com.kenzie.appserver.controller.model.StudyGroupReviewResponse;
import com.kenzie.appserver.service.StudyGroupReviewService;
import com.kenzie.appserver.service.model.StudyGroupReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.*;


@RestController
@RequestMapping("/v1")
public class StudyGroupReviewController {

    @Autowired
    private StudyGroupReviewService reviewService;

    public StudyGroupReviewController(StudyGroupReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/studygroup/reviews")
    public ResponseEntity<StudyGroupReviewResponse> submitReview(@RequestBody StudyGroupReviewRequest request) {
        if (request.getGroupId() == null || request.getGroupId().length() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }

        StudyGroupReview studyGroupReview = convertToReview(request);
        StudyGroupReview review = reviewService.submitStudyGroupReview(studyGroupReview);
        StudyGroupReviewResponse response = convertToResponse(review);
        return ResponseEntity.created(URI.create("/studygroup/reviews" + response.getReviewId())).body(response);

    }

    @GetMapping("/studygroup/reviews/{reviewId}")
    public ResponseEntity<StudyGroupReviewResponse> getReviewByReviewId(@PathVariable String reviewId) {
        StudyGroupReview review = reviewService.getStudyGroupReview(reviewId);
        if (review != null) {
            StudyGroupReviewResponse response = convertToResponse(review);
            return ResponseEntity.status(200).body(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/studygroup/reviews/discussionTopic/{discussionTopic}")
    public ResponseEntity<List<GroupReviewResponse>> getStudyGroupReviewsByTopic(@PathVariable String discussionTopic)  {

        List<StudyGroupReview> reviewsByTopic = reviewService.getStudyGroupReviewsByTopic(discussionTopic);

        if (reviewsByTopic == null || reviewsByTopic.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<GroupReviewResponse> reviewResponseList = new ArrayList<>();
        if (reviewsByTopic != null) {
            for (StudyGroupReview review : reviewsByTopic) {
                reviewResponseList.add(reviewToResponses(review));
            }
        }
        return ResponseEntity
                .status(200)
                .body(reviewResponseList);
    }

    /**
     * Vicky - implement this one
     */
    @GetMapping("/studygroup/ratings/{averageRating}/{discussionTopic}")
    public ResponseEntity<Map<String, ?>> getGroupsWithDesiredAvgRatingByTopic(@PathVariable double averageRating, @PathVariable String discussionTopic) {
        Map<String, Double> groupsWithDesiredRating = reviewService.getGroupsWithDesiredRating(averageRating,discussionTopic);

        if(groupsWithDesiredRating == null || groupsWithDesiredRating.isEmpty()){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Desired rating not found for topic: " + discussionTopic);
            return ResponseEntity.status(400).body(errorResponse);
        }
        return ResponseEntity
                .status(200)
                .body(groupsWithDesiredRating);
    }

    @GetMapping("/studygroup/averageRating/{groupId}")
    public ResponseEntity<Double> getAverageRatingById(@PathVariable String groupId){
        double averageRating = reviewService.calculateAverageRating(groupId);
        return ResponseEntity.ok(averageRating);
    }

    private StudyGroupReview convertToReview(StudyGroupReviewRequest request) {
        StudyGroupReview review = new StudyGroupReview();
        review.setGroupId(request.getGroupId());
        review.setReviewId(UUID.randomUUID().toString());
        review.setGroupName(request.getGroupName());
        review.setDiscussionTopic(request.getDiscussionTopic());
        review.setRating(request.getRating());
        review.setReviewComments(request.getReviewComment());

        return review;
    }

    private StudyGroupReviewResponse convertToResponse(StudyGroupReview review) {
        StudyGroupReviewResponse response = new StudyGroupReviewResponse();
        response.setGroupId(review.getGroupId());
        response.setGroupName(review.getGroupName());
        response.setReviewId(review.getReviewId());
        response.setRating(review.getRating());
        response.setDiscussionTopic(review.getDiscussionTopic());
        response.setAverageRating(review.getAverageRating());
        response.setReviewComment(review.getReviewComments());
        return response;
    }

    private GroupReviewResponse reviewToResponses(StudyGroupReview review) {
        GroupReviewResponse response = new GroupReviewResponse();
        response.setGroupId(review.getGroupId());
        response.setGroupName(review.getGroupName());
        response.setReviewId(review.getReviewId());
        response.setTotalRating(review.getRating());
        response.setDiscussionTopic(review.getDiscussionTopic());
        response.setAverageRating(review.getAverageRating());
        response.setReviewComments(review.getReviewComments());
        return response;
    }


}
