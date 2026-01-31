package me.miran.mhdstuff.activity.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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

    @Composable
    fun Container(modifier: Modifier = Modifier, innerPadding: Dp = 16.dp, content: @Composable BoxScope.() -> Unit) {
        Surface(shape = RoundedCornerShape(size = 16.dp), color = colorResource(R.color.widget_background), modifier = modifier.fillMaxWidth()) {
            Box(Modifier.padding(innerPadding), content= content);
        }
    }

    @Composable
    fun Divider() {
        HorizontalDivider(thickness = 1.dp, color = colorResource(R.color.mid_gray))
    }


}