package dev.rikoapp.cleanphonelauncher.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.rikoapp.cleanphonelauncher.domain.AppData
import dev.rikoapp.cleanphonelauncher.presentation.ui.theme.CloseIcon
import kotlinx.coroutines.launch

@Composable
fun AppListScreen(apps: List<AppData>, isActive: Boolean) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val filteredApps = if (searchText.isBlank()) {
        apps
    } else {
        apps.filter { it.name.startsWith(searchText, ignoreCase = true) }
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
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { searchText = "" }) {
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
                    AppRow(app = app)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp)
                    .width(24.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, _ ->
                            val y = change.position.y
                            val letterIndex = (y / (size.height / alphabet.length)).toInt()
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
                alphabet.forEach { letter ->
                    Text(
                        text = letter.toString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            searchText = ""
                            val index = if (letter == '#') {
                                apps.indexOfFirst { !it.name[0].isLetter() }
                            } else {
                                apps.indexOfFirst { it.name.startsWith(letter, ignoreCase = true) }
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


@Composable
private fun AppRow(app: AppData) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.small)
            .clickable {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                context.startActivity(intent)
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = app.name,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
