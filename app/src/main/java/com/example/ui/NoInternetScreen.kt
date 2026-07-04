package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.Translation
import com.example.ui.theme.*

@Composable
fun NoInternetScreen(
    viewModel: MainViewModel,
    appLanguage: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "No Internet Connection",
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Translation.translate("no_internet", appLanguage),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("no_internet_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Please check your network and cellular settings and try again.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.checkNetwork() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CosmicAccentTeal,
                    contentColor = CosmicDeepSpace
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp)
                    .testTag("retry_connection_button")
            ) {
                Text(
                    text = Translation.translate("retry", appLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
