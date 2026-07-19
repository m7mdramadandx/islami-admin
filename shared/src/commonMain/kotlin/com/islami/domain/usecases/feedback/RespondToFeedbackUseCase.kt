package com.islami.domain.usecases.feedback

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.repositories.FeedbackRepository

class RespondToFeedbackUseCase(
    private val feedbackRepository: FeedbackRepository
) : UseCase<RespondToFeedbackParams, Unit>() {
    override suspend fun invoke(params: RespondToFeedbackParams): Result<Unit> {
        return feedbackRepository.respondToFeedback(params.id, params.response)
    }
}

data class RespondToFeedbackParams(val id: String, val response: String)
