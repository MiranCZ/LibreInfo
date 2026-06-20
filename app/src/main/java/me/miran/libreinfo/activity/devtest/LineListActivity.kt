package me.miran.libreinfo.activity.devtest

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.storage.manager.AppContainer
import me.miran.libreinfo.parsing.storage.manager.IdStorage

class LineListActivity : KBaseActivity(R.string.dev_settings) {
    @Composable
    override fun CreateElements() {
        // TODO get only line storage?

        var storage: IdStorage? by remember { mutableStateOf(AppContainer.storageProvider.getInstanceOrNull()) }
        LaunchedEffect(Unit) {
            val s = withContext(Dispatchers.IO) {
                AppContainer.storageProvider.getInstance()
            }

            storage = s
        }

        if (storage != null) {
            val items = storage!!.lineStorage.allAliases

            Container(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), innerPadding = 4.dp) {
                LineList(items)
            }
        }
    }


}