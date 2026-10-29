package com.islami.domain.usecases.feedback

import com.islami.core.error.Result
import com.islami.core.usecase.FlowUseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.FeedbackMessage
import com.islami.domain.repositories.FeedbackRepository
import kotlinx.coroutines.flow.Flow

class ObserveFeedbackMessagesUseCase(
    private val feedbackRepository: FeedbackRepository
) : FlowUseCase<NoParams, List<FeedbackMessage>>() {
    override fun invoke(params: NoParams): Flow<Result<List<FeedbackMessage>>> {
        return feedbackRepository.observeFeedbackMessages()
    }
}
