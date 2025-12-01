package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

/**
 * Skeleton loader for a task item using compose-shimmer library
 */
@Composable
fun TaskItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shimmer(), // Apply shimmer effect from library
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Priority border placeholder
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(120.dp)
                    .background(Color.LightGray)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Title and priority badge row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title placeholder
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .background(Color.LightGray, RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Priority badge placeholder
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(24.dp)
                                .background(Color.LightGray, RoundedCornerShape(4.dp))
                        )
                    }

                    // Description placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(16.dp)
                            .background(Color.LightGray, RoundedCornerShape(4.dp))
                    )

                    // Due date placeholder
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(14.dp)
                            .background(Color.LightGray, RoundedCornerShape(4.dp))
                    )

                    // Status badge placeholder
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(22.dp)
                            .background(Color.LightGray, RoundedCornerShape(4.dp))
                    )
                }

                // Checkbox placeholder
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.LightGray, CircleShape)
                )
            }
        }
    }
}

/**
 * Skeleton loader for project item using compose-shimmer library
 */
@Composable
fun ProjectItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shimmer(), // Apply shimmer effect from library
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Project name placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(24.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp))
            )

            // Description placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp))
            )

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .background(Color.LightGray, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}