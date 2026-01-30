package me.miran.mhdstuff.activity.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.miran.mhdstuff.R
import me.miran.mhdstuff.ui.theme.AppTheme
import java.util.function.Consumer
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
abstract class KBaseActivity(var nameId: Int) : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(this.nameId),
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        },
                        navigationIcon = {
                            if (parentActivityIntent != null) {
                                IconButton(onClick = { onBackPressed() }) {
                                    Icon(
                                        painterResource(R.drawable.chevron_left),
                                        "Go back",
                                        tint = colorResource(R.color.light_blue)
                                    )
                                }
                            }
                        }
                    )
                }) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        CreateElements()
                    }
                }
            }
        }
    }

    @Composable
    abstract fun CreateElements();

    fun startActivity(clazz: KClass<out Activity>) {
        startActivity(clazz) {}
    }

    fun startActivity(clazz: KClass<out Activity>, intentSetup: Consumer<Intent>) {
        val intent = Intent(this, clazz.java)

        intentSetup.accept(intent)
        startActivity(intent)
        overridePendingTransition(R.anim.fast_scale_up, R.anim.fast_fade_out)
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down)
    }

    override fun onNavigateUp(): Boolean {
        val result = super.onNavigateUp()
        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_scale_down)
        return result
    }


}