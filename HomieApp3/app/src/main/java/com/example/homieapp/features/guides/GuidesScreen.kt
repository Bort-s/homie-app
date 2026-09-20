package com.example.homieapp.features.guides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homieapp.BlockGuide
import com.example.homieapp.R
import com.example.homieapp.core.ui.theme.HomieAppTheme
import kotlinx.coroutines.launch

@Composable
fun GuideScreen(guidePage: Int, onNavigateToHome: () -> Unit, addPage: () -> Unit, restPage: () -> Unit) {
    val viewmodel: GuidesViewModel = viewModel()
    val allGuides by viewmodel.listGuides.collectAsState()
    var currentGuide by remember { mutableIntStateOf(guidePage) }
    val guide = allGuides[currentGuide]

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxSize()
        ) {
            // Header
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()) {
                    IconButton(onClick = onNavigateToHome
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Regresar",
                            tint = Color(0xFF0055d4),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        "Guia de Usuario",
                        color = Color(0xFF0055d4),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0055d4).copy(alpha = 0.1f))
                ) {
                    Text(
                        "${guide.chapter} ● CAPITULO 0${guide.number}",
                        color = Color(0xFF0055d4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp)

                    )
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                item {
                    Text(
                        "Capitulo ${guide.number}: ",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 42.sp
                    )
                    Text(
                        guide.title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 42.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(guide.blocks) { bloque ->
                    BlockGuide(bloque)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            if (currentGuide > 0) {
                                restPage()
                                currentGuide -= 1
                            }
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF525252)
                            ),
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back_ios),
                                    contentDescription = "Regresar",
                                )
                                Text(
                                    "Regresar\nCapitulo",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                        Button(onClick = {
                            if (currentGuide < allGuides.size - 1) {
                                addPage()
                                currentGuide += 1
                            }
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0055d4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            enabled = currentGuide < allGuides.size - 1,
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Text(
                                    "Siguiente\nCapitulo",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_forward_ios),
                                    contentDescription = "Siguiente",
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GuideScreenPreview() {
    HomieAppTheme {
        GuideScreen(1, onNavigateToHome = {}, addPage = {}, restPage = {})
    }
}