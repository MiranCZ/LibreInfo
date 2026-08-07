package io.github.mirancz.libreinfo.activity.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.parsing.types.stop.Stop

class StopViewModel : ViewModel() {
    private val _liked = mutableStateOf(false)
    private val _isRefreshing = mutableStateOf(false)
    val liked = _liked
    val refreshing = _isRefreshing

    fun toggleLiked() {
        _liked.value = !_liked.value
    }

    fun setLiked(value: Boolean) {
        _liked.value = value
    }

    fun setRefreshing(value: Boolean) {
        _isRefreshing.value = value
    }
}

/** Toolbar heart that marks [stop] as a favourite. */
@Composable
fun FavouriteStopAction(stop: Stop) {
    val vm: StopViewModel = viewModel()
    val liked by vm.liked

    IconButton(onClick = {
        vm.toggleLiked()
        stop.setFavourite(liked)
        stop.flush()
    }) {
        if (liked) {
            Icon(
                painter = painterResource(R.drawable.heart_solid),
                contentDescription = "Unlike",
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.heart_regular),
                contentDescription = "Like",
                tint = colorResource(R.color.light_blue),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
