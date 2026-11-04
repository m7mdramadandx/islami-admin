package com.islami.domain.usecases.content

import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentKind
import com.islami.domain.entities.ContentSnapshot

data class PublishContentParams(
    val kind: ContentKind,
    val document: ContentDocument,
    val base: ContentSnapshot,
    /** Publish even though someone else changed the live file since [base] was loaded. */
    val overwrite: Boolean = false
)
