package com.rogers.seekr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val contentApi by lazy {
        ContentApi(
            "https://www.seekyoursound.com/wp-json/ang/v1",
            client()
        )
    }

    private val liveApi by lazy {
        LiveApi(
            "https://radio.rogersdigitalmedia.com/",
            client()
        )
    }

    init {
        fetchStations()
    }

    private fun fetchStations() {
        viewModelScope.launch {
            val stationsResponse = contentApi.getRadioStations(
                null,
                null,
                null,
                null,
                null,
                null
            )

            val nowPlayingResponse = liveApi.getNowPlaying()

            val nowPlaying = when (nowPlayingResponse.success) {
                true -> nowPlayingResponse.body()
                false -> emptyList() //TODO: Handle error
            }

            val radioStations = when (stationsResponse.success) {
                true -> stationsResponse.body()
                false -> emptyList() //TODO: Handle error
            }

            val stations = radioStations.map { station ->

                val stationNowPlaying = nowPlaying.find { it.callLetters == station.callLetters }

                val thumbnailUrl = stationNowPlaying?.nowPlaying?.image?.thumbnail?.src
                    ?: station.image.thumbnail.src

                Station(
                    station.name,
                    stationNowPlaying?.nowPlaying?.title ?: "N/A",
                    thumbnailUrl,
                )
            }

            _stations.value = stations
        }
    }
}