package com.islami.domain.usecases.images

import com.islami.domain.entities.ImageFolder

class UploadImageParams(val folder: ImageFolder, val bytes: ByteArray)
