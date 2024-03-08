package com.teamsparta.buysell.domain.review.controller

import com.teamsparta.buysell.domain.common.dto.MessageResponse
import com.teamsparta.buysell.domain.review.dto.request.CreateReviewRequest
import com.teamsparta.buysell.domain.review.dto.request.UpdateReviewRequest
import com.teamsparta.buysell.domain.review.service.ReviewService
import com.teamsparta.buysell.infra.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/posts/{postId}/reviews")
@RestController
class ReviewController(
    private val reviewService: ReviewService
) {
    @PostMapping
    fun createReview(
        @PathVariable postId: Int,
        @Valid @RequestBody request: CreateReviewRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<MessageResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(reviewService.createReview(postId, request, principal))
    }

    @PutMapping("/{reviewId}")
    fun editReview(
        @PathVariable reviewId: Int,
        @PathVariable postId: Int,
        @RequestBody request: UpdateReviewRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<MessageResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(reviewService.editReview(reviewId, postId, request, principal))
    }

    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @PathVariable postId: Int,
        @PathVariable reviewId: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<MessageResponse> {
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(reviewService.deleteReview(postId, reviewId, principal))
    }

}