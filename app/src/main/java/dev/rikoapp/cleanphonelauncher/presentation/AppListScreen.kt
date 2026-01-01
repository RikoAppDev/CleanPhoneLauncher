package dev.rikoapp.cleanphonelauncher.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.rikoapp.cleanphonelauncher.domain.AppData
import dev.rikoapp.cleanphonelauncher.presentation.components.AppListItem
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CleanPhoneLauncherTheme
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppListScreen(
    apps: List<AppData>,
    isActive: Boolean
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val filteredApps = if (searchText.isBlank()) {
        apps
    } else {
        apps
            .filter { it.name.contains(searchText, ignoreCase = true) }
            .sortedWith(
                compareBy<AppData> { it.name.indexOf(searchText, ignoreCase = true) }
                    .thenBy { it.name.length }
            )
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .focusRequester(focusRequester),
            placeholder = { Text("Search apps...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (filteredApps.isNotEmpty() && searchText.isNotBlank()) {
                        val appToLaunch = filteredApps.first()
                        val intent =
                            context.packageManager.getLaunchIntentForPackage(appToLaunch.packageName)
                        context.startActivity(intent)
                        focusManager.clearFocus()
                    }
                }
            ),
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchText = ""
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(imageVector = CloseIcon, contentDescription = "Clear search")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filteredApps) { app ->
                    AppListItem(app = app)
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp)
                    .width(24.dp)
            ) {
                val columnHeight = this.maxHeight
                val density = LocalDensity.current
                val letterTextHeight = 14.sp // Font size is 12.sp, adding some padding

                val letterTextHeightPx = with(density) { letterTextHeight.toPx() }
                val maxLetters = (columnHeight.value / letterTextHeightPx).toInt()

                val displayedAlphabet = remember(maxLetters, alphabet) {
                    if (maxLetters <= 0) {
                        ""
                    } else if (maxLetters >= alphabet.length) {
                        alphabet
                    } else {
                        val step = (alphabet.length - 1).toFloat() / (maxLetters - 1)
                        (0 until maxLetters).map { i ->
                            alphabet[(i * step).roundToInt()]
                        }.distinct().joinToString("")
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, _ ->
                                val y = change.position.y
                                val letterIndex = (y / (size.height / alphabet.length))
                                    .toInt()
                                    .coerceIn(alphabet.indices)
                                if (letterIndex in alphabet.indices) {
                                    searchText = ""
                                    val letter = alphabet[letterIndex]
                                    val index = if (letter == '#') {
                                        apps.indexOfFirst { !it.name[0].isLetter() }
                                    } else {
                                        apps.indexOfFirst {
                                            it.name.startsWith(
                                                letter,
                                                ignoreCase = true
                                            )
                                        }
                                    }
                                    if (index != -1) {
                                        coroutineScope.launch {
                                            listState.scrollToItem(index)
                                        }
                                    }
                                }
                            }
                        },
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    displayedAlphabet.forEach { letter ->
                        Text(
                            text = letter.toString(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                focusManager.clearFocus()
                                searchText = ""
                                val index = if (letter == '#') {
                                    apps.indexOfFirst { !it.name[0].isLetter() }
                                } else {
                                    apps.indexOfFirst {
                                        it.name.startsWith(
                                            letter,
                                            ignoreCase = true
                                        )
                                    }
                                }
                                if (index != -1) {
                                    coroutineScope.launch {
                                        listState.scrollToItem(index)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AppListPreview() {
    CleanPhoneLauncherTheme {
        AppListScreen(
            apps = listOf(
                AppData(
                    name = "WhatsApp",
                    packageName = "com.whatsapp"
                ),
                AppData(
                    name = "Camera",
                    packageName = "com.google.android.apps.camera"
                ),
                AppData(
                    name = "Discord",
                    packageName = "com.discord"
                ),
                AppData(
                    name = "Telegram",
                    packageName = "org.telegram.messenger"
                ),
                AppData(
                    name = "Facebook",
                    packageName = "com.facebook.katana"
                ),
                AppData(
                    name = "Instagram",
                    packageName = "com.instagram.android"
                ),
                AppData(
                    name = "Twitter",
                    packageName = "com.twitter.android"
                ),
                AppData(
                    name = "Reddit",
                    packageName = "com.reddit.app"
                ),
                AppData(
                    name = "YouTube",
                    packageName = "com.google.android.youtube"
                ),
                AppData(
                    name = "Spotify",
                    packageName = "com.spotify.music"
                ),
                AppData(
                    name = "Netflix",
                    packageName = "com.netflix.mediaclient"
                ),
                AppData(
                    name = "Amazon Prime Video",
                    packageName = "com.amazon.avod"
                ),
            ),
            isActive = true
        )
    }
}
