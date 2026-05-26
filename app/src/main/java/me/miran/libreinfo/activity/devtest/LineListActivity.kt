package me.miran.libreinfo.activity.devtest

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.storage.IdStorage

class LineListActivity : KBaseActivity(R.string.dev_settings) {
    @Composable
    override fun CreateElements() {
        // TODO get only line storage?
        IdStorage.getInstanceOnUIThread({ storage: IdStorage ->
            val items = storage.lineStorage.allAliases

            setBaseContent {
                Container(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), innerPadding = 4.dp) {
                    LineList(items)
                }
            }
        }, this)
    }


}