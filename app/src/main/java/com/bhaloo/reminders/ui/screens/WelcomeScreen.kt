package com.bhaloo.reminders.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.data.Maker
import com.bhaloo.reminders.ui.theme.Glass
import com.bhaloo.reminders.ui.theme.GlassPane
import com.bhaloo.reminders.ui.theme.MeshBackground
import com.bhaloo.reminders.ui.theme.glassInk
import com.bhaloo.reminders.ui.theme.glassInkSoft
import kotlinx.coroutines.delay

/**
 * The starting interface: a single pane of glass floating on the mesh, holding
 * their face and the reason this app exists.
 */
@Composable
fun WelcomeScreen(onDone: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.9f,
        animationSpec = tween(700),
        label = "welcome-scale"
    )
    val fade by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(600),
        label = "welcome-fade"
    )

    LaunchedEffect(Unit) {
        shown = true
        delay(2400)
        onDone()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // The overlay carries its own backdrop: it sits above the app's.
        MeshBackground()

        GlassPane(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 30.dp)
                .fillMaxWidth()
                .scale(scale)
                .alpha(fade),
            elevation = 30.dp,
            iridescent = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.bhaloo_portrait),
                    contentDescription = stringResource(R.string.bhaloo_photo_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(146.dp)
                        .shadow(24.dp, CircleShape, clip = false, spotColor = Glass.shadowViolet)
                        .clip(CircleShape)
                        .border(3.dp, Glass.iridescentRim, CircleShape)
                )
                Spacer(Modifier.height(26.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = glassInk(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.dedication_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = glassInkSoft(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.dedication_tagline_hi),
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassInkSoft(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.made_by_line, Maker.NAME),
                    style = MaterialTheme.typography.labelLarge,
                    color = glassInkSoft(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
