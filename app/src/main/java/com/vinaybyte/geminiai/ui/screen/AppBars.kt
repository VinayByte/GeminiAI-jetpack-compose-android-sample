package com.vinaybyte.geminiai.ui.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vinaybyte.geminiai.R

/**
 * @Author: Vinay
 * @Date: 15-01-2024
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBars(title: String = stringResource(R.string.app_name)) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(title)
        }
    )
}