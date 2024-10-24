package com.rogers.seekr

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.crossfade
import coil3.util.DebugLogger

typealias ContentApi = com.rogers.seekr.api.content.DefaultApi
typealias LiveApi = com.rogers.seekr.api.live.DefaultApi

fun getAsyncImageLoader(context: PlatformContext)=
    ImageLoader.Builder(context)
        .crossfade(true)
        .logger(DebugLogger())
        .build()