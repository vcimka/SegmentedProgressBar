package com.vcimka.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.vcimka.sample.ui.theme.SegmentedProgressBarTheme
import com.vcimka.segmentedprogressbar.SegmentedProgressBar
import kotlin.system.measureTimeMillis

private const val STORY_DURATION = 3000L
private const val CLICK_DELAY = 300L

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SegmentedProgressBarTheme {

                val vm by viewModels<MainViewModel>()
                val state = vm.uiState.collectAsState().value

                val direction = remember { mutableStateOf(LayoutDirection.Ltr) }

                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                ) {

                    Tools(vm, state, direction)

                    CompositionLocalProvider(LocalLayoutDirection provides direction.value) {
                        StoryScreenSample(vm, state)
                    }
                }
            }
        }
    }

    @Composable
    fun Tools(
        vm: MainViewModel,
        state: State,
        direction: MutableState<LayoutDirection>
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Text(text = "SegmentsCount:", color = Color.White)
            TextField(
                value = state.segmentsCount.toString(),
                onValueChange = { vm.storiesCountChanged(it.toIntOrNull() ?: 1) },
                textStyle = TextStyle(color = Color.White)
            )
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Text(text = "Direction:", color = Color.White)
            Button(
                onClick = {
                    direction.value =
                        if (direction.value == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
                }
            ) {
                Text("${direction.value.name}")
            }
        }
    }


    @Composable
    fun StoryScreenSample(vm: MainViewModel, state: State) {

        val currentStory = remember(state.currentStoryIndex) { state.currentStory } ?: return
        val isPaused = remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {

                    detectTapGestures(
                        onPress = {
                            val pressDuration = measureTimeMillis {
                                isPaused.value = true
                                awaitRelease()
                                isPaused.value = false
                            }
                            if (pressDuration <= CLICK_DELAY) vm.nextStory()
                        }
                    )
                }
        ) {

            StoryContent(
                title = currentStory.first,
                color = currentStory.second,
                onPrevClick = vm::prevStory,
                onNextClick = vm::nextStory
            )

            SegmentedProgressBar(
                modifier = Modifier
                    .padding(top = 47.dp, start = 18.dp, end = 18.dp)
                    .fillMaxWidth()
                    .height(4.dp),
                segmentCount = state.stories.size,
                currentSegmentIndex = state.currentStoryIndex,
                isPaused = isPaused.value,
                gap = 5.dp,
                cornerRadius = 2.dp,
                segmentDuration = STORY_DURATION,
                onSegmentFilled = { vm.nextStory() },
                onFinished = vm::onAllStoriesDone
            )
        }
    }

    @Composable
    fun StoryContent(
        title: String,
        color: Color,
        onPrevClick: () -> Unit,
        onNextClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(color),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onPrevClick) {
                Text(text = "Previous")
            }
            Text(text = title, color = Color.White)
            Button(onClick = onNextClick) {
                Text(text = "Next")
            }
        }
    }
}