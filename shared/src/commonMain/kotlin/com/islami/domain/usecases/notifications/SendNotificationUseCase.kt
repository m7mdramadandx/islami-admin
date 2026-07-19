package com.islami.domain.usecases.notifications

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.Notification
import com.islami.domain.repositories.NotificationRepository

class SendNotificationUseCase(
    private val notificationRepository: NotificationRepository
) : UseCase<Notification, Unit>() {
    override suspend fun invoke(params: Notification): Result<Unit> {
        return notificationRepository.sendNotification(params)
    }
}
