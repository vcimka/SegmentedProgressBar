package com.vcimka.sample

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

data class State(
    val currentStoryIndex: Int = 0,
    val segmentsCount: Int = 5,
    val stories: List<Pair<String, Color>> = segmentsCount.generateStories()
) {

    val currentStory get() = stories.getOrNull(currentStoryIndex)
}

private fun Int.generateStories() = (0 until this).map { "story${it.inc()}" to RandomColor }

private val RandomColor
    get() = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

class MainViewModel : ViewModel() {

    val uiState = MutableStateFlow(
        State(
            currentStoryIndex = 0,
            segmentsCount = 5
        )
    )

    fun storiesCountChanged(newCount: Int) {
        uiState.value = uiState.value.copy(
            currentStoryIndex = 0,
            segmentsCount = newCount,
            stories = newCount.generateStories()
        )
    }

    fun nextStory() {
        uiState.value = uiState.value.let {
            it.copy(currentStoryIndex = it.currentStoryIndex.inc().coerceIn(it.stories.indices))
        }
    }

    fun prevStory() {
        uiState.value = uiState.value.let {
            it.copy(currentStoryIndex = it.currentStoryIndex.dec().coerceIn(it.stories.indices))
        }
    }

    fun onAllStoriesDone() {
        Log.e("AAAA", "Done")
    }
}