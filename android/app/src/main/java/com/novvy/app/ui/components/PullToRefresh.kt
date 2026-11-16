package com.novvy.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

/**
 * Lightweight pull-to-refresh for a scrolling column.
 * Drag down at the top past the threshold to trigger [onRefresh].
 */
@Composable
fun PullToRefreshColumn(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    var drag by remember { mutableFloatStateOf(0f) }
    var triggered by remember { mutableStateOf(false) }

    val connection = remember(scrollState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && scrollState.value == 0) {
                    if (available.y > 0f) {
                        val next = (drag + available.y).coerceIn(0f, 140f)
                        val consumed = next - drag
                        drag = next
                        if (drag >= 90f && !triggered) {
                            triggered = true
                            onRefresh()
                        }
                        return Offset(0f, consumed)
                    } else if (available.y < 0f) {
                        drag = 0f
                        triggered = false
                    }
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            drag = 0f
            triggered = false
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.verticalScroll(scrollState).nestedScroll(connection)) {
            content()
        }
        if (drag > 0f || isRefreshing) {
            Box(
                Modifier.align(Alignment.TopCenter)
                    .padding(0.dp, (drag - 46f).coerceAtLeast(0f).dp, 0.dp, 0.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}