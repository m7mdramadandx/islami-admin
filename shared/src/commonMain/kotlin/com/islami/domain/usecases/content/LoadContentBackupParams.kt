package com.islami.domain.usecases.content

import com.islami.domain.entities.ContentBackup
import com.islami.domain.entities.ContentKind

data class LoadContentBackupParams(val kind: ContentKind, val backup: ContentBackup)
