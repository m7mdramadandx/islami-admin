package com.islami.domain.usecases.feedback

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.FeedbackMessage
import com.islami.domain.repositories.FeedbackRepository

class GetFeedbackMessagesUseCase(
    private val feedbackRepository: FeedbackRepository
) : UseCase<Int, List<FeedbackMessage>>() {
    override suspend fun invoke(params: Int): Result<List<FeedbackMessage>> {
        return feedbackRepository.getFeedbackMessages(params)
    }
}
