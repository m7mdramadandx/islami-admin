package com.islami.domain.usecases.feedback

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.repositories.FeedbackRepository

class DeleteFeedbackMessageUseCase(
    private val feedbackRepository: FeedbackRepository
) : UseCase<String, Unit>() {
    override suspend fun invoke(params: String): Result<Unit> {
        return feedbackRepository.deleteFeedbackMessage(params)
    }
}
