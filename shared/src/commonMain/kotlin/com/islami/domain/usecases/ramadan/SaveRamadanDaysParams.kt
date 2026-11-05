package com.islami.domain.usecases.ramadan

import com.islami.domain.entities.RamadanDay

data class SaveRamadanDaysParams(
    val days: List<RamadanDay>,
    val base: List<RamadanDay>,
    /** Save even though someone else changed one of these days since [base] was loaded. */
    val overwrite: Boolean = false
)
