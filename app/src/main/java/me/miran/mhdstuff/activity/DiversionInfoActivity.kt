package me.miran.mhdstuff.activity

import android.util.TypedValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.parsing.types.Diversion

class DiversionInfoActivity : KBaseActivity(R.string.diversions) {
    @Composable
    override fun CreateElements() {
        val diversion = intent.getParcelableExtra("diversion", Diversion::class.java)

        val context = LocalContext.current
        if (diversion != null) {
            Container(
                Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                EventHeader(diversion) {
                    HTML(Modifier.padding(top = 16.dp), diversion.publicText) { tv ->
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                            tv.setTextColor(ContextCompat.getColor(context, R.color.secondaryColor))
                    }

                }
            }
        } else {
            NothingHere()
        }
    }


}