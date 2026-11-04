package com.islami.core.di

import com.islami.data.local.LocalDataSource
import com.islami.data.local.LocalDataSourceImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<LocalDataSource> { LocalDataSourceImpl() }
}
