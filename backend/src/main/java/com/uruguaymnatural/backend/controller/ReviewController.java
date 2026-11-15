package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.MyReviewDto;
import com.uruguaymnatural.backend.dto.ReviewDto;
import com.uruguaymnatural.backend.dto.ReviewReplyDto;
import com.uruguaymnatural.backend.dto.ReviewRequestDto;
import com.uruguaymnatural.backend.security.AuthUser;
import com.uruguaymnatural.backend.service.ReviewService;

import jakarta.validation.Valid;

/**
 * Reviews: tourists write them, providers answer them. Reading them is part of the public
 * detail of the activity (GET /api/activities/{id}).
 */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** GET /api/tourist/activities/{id}/review — can I review this activity, and my review if I wrote one. */
    @GetMapping("/tourist/activities/{id}/review")
    public MyReviewDto getOwn(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id) {
        return reviewService.getOwn(user.id(), id);
    }

    /** POST /api/tourist/activities/{id}/reviews — reviews an activity (CU-20). 201, or 409 if I did not take part. */
    @PostMapping("/tourist/activities/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto data) {
        return reviewService.create(user.id(), id, data);
    }

    /** PUT /api/tourist/reviews/{id} — edits my review. */
    @PutMapping("/tourist/reviews/{id}")
    public ReviewDto update(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto data) {
        return reviewService.update(user.id(), id, data);
    }

    /** DELETE /api/tourist/reviews/{id} — deletes my review. 204. */
    @DeleteMapping("/tourist/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id) {
        reviewService.delete(user.id(), id);
    }

    /** GET /api/provider/activities/{id}/reviews — the reviews of one of my activities. */
    @GetMapping("/provider/activities/{id}/reviews")
    public List<ReviewDto> listOfOwnActivity(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user,
            @PathVariable Long id) {
        return reviewService.listOfOwnActivity(user.id(), id);
    }

    /** PUT /api/provider/reviews/{id}/reply — answers a comment of one of my activities (CU-21). */
    @PutMapping("/provider/reviews/{id}/reply")
    public ReviewDto reply(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id,
            @Valid @RequestBody ReviewReplyDto data) {
        return reviewService.reply(user.id(), id, data.reply());
    }
}
