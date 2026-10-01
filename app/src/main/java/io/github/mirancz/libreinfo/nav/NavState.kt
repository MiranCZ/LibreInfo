package io.github.mirancz.libreinfo.nav

data class NavState(val onNavigate: (NavRoute) -> Unit, val onBack: () -> Unit)