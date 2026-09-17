package com.bhaloo.reminders.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaloo.reminders.R
import com.bhaloo.reminders.ui.ReminderViewModel
import com.bhaloo.reminders.util.BhalooWords

/**
 * The dedication page. Everything here exists to say one thing:
 * this app was made for Bhaloo, by hand, on purpose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: ReminderViewModel, onClose: () -> Unit) {
    val store = viewModel.store
    val reminders by viewModel.reminders.collectAsState()
    var taps by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.bhaloo_portrait),
                contentDescription = stringResource(R.string.bhaloo_photo_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .clickable { taps++ }
            )

            Text(
                stringResource(R.string.dedication_headline),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.dedication_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.dedication_body_hi),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.stats_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(10.dp))
                    StatRow(
                        label = stringResource(R.string.stats_times),
                        value = store.timesReminded.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.stats_active),
                        value = reminders.count { it.enabled }.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.stats_completed),
                        value = reminders.sumOf { it.timesCompleted }.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.stats_days),
                        value = BhalooWords.daysTogether(store.installedAt).toString()
                    )
                }
            }

            val signature = store.madeBy
            Text(
                text = if (signature.isBlank()) {
                    stringResource(R.string.signature_anonymous)
                } else {
                    stringResource(R.string.signature_named, signature)
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            if (taps >= 5) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.easter_egg_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.easter_egg_body),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                Text(
                    stringResource(R.string.easter_egg_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                stringResource(R.string.version_line),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}
