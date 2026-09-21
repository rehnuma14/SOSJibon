package com.example.sosjibon.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

@Composable
fun FloatingAiBubble(
    onClick: () -> Unit
) {

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {

        val bubbleSize = 62.dp

        val sidePadding = 16.dp

        /*
         * Space above the bottom navigation bar.
         */
        val bottomPadding = 100.dp

        val density = LocalDensity.current

        val screenWidthPx =
            with(density) {
                maxWidth.toPx()
            }

        val screenHeightPx =
            with(density) {
                maxHeight.toPx()
            }

        val bubbleSizePx =
            with(density) {
                bubbleSize.toPx()
            }

        val sidePaddingPx =
            with(density) {
                sidePadding.toPx()
            }

        val bottomPaddingPx =
            with(density) {
                bottomPadding.toPx()
            }

        /*
         * Initial position:
         *
         * LEFT
         * LOWER SIDE
         *
         * Easy for thumb access.
         */
        val initialX =
            sidePaddingPx

        val initialY =
            (
                    screenHeightPx -
                            bubbleSizePx -
                            bottomPaddingPx
                    ).coerceAtLeast(sidePaddingPx)

        var offsetX by remember {
            mutableFloatStateOf(initialX)
        }

        var offsetY by remember {
            mutableFloatStateOf(initialY)
        }

        /*
         * Horizontal boundaries.
         */
        val minX =
            sidePaddingPx

        val maxX =
            (
                    screenWidthPx -
                            bubbleSizePx -
                            sidePaddingPx
                    ).coerceAtLeast(minX)

        /*
         * Vertical boundaries.
         */
        val minY =
            sidePaddingPx

        val maxY =
            (
                    screenHeightPx -
                            bubbleSizePx -
                            bottomPaddingPx
                    ).coerceAtLeast(minY)

        Box(
            modifier = Modifier
                .offset {

                    IntOffset(
                        offsetX.roundToInt(),
                        offsetY.roundToInt()
                    )
                }

                .size(bubbleSize)

                .clip(CircleShape)

                .background(
                    MaterialTheme
                        .colorScheme
                        .primary
                )

                /*
                 * Drag gesture.
                 *
                 * The button stays inside
                 * the screen boundaries.
                 */
                .pointerInput(
                    screenWidthPx,
                    screenHeightPx
                ) {

                    detectDragGestures(

                        onDrag = {
                                change,
                                dragAmount ->

                            change.consume()

                            offsetX =
                                (
                                        offsetX +
                                                dragAmount.x
                                        ).coerceIn(
                                        minX,
                                        maxX
                                    )

                            offsetY =
                                (
                                        offsetY +
                                                dragAmount.y
                                        ).coerceIn(
                                        minY,
                                        maxY
                                    )
                        },

                        onDragEnd = {

                            /*
                             * Snap to the closest
                             * horizontal edge.
                             */
                            val bubbleCenterX =
                                offsetX +
                                        bubbleSizePx / 2f

                            val screenCenterX =
                                screenWidthPx / 2f

                            offsetX =
                                if (
                                    bubbleCenterX <
                                    screenCenterX
                                ) {

                                    minX

                                } else {

                                    maxX
                                }
                        }
                    )
                },

            contentAlignment =
                Alignment.Center
        ) {

            /*
             * IMPORTANT
             *
             * We intentionally use a clickable
             * Icon here instead of putting
             * clickable() on the parent.
             *
             * This avoids the unresolved
             * clickable problem and gives
             * the AI button a real click target.
             */
            Icon(
                imageVector =
                    Icons.Filled.AutoAwesome,

                contentDescription =
                    "SOS Jibon AI Assistant",

                tint =
                    Color.White,

                modifier =
                    Modifier
                        .size(30.dp)
                        .clickable(
                            onClick = onClick
                        )
            )
        }
    }
}