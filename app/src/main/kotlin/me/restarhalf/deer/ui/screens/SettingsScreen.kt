package me.restarhalf.deer.ui.screens


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow

@Composable
fun SettingsScreen(
    navController: NavController
) {
    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "设置",
                color = Color.Transparent,
                scrollBehavior = MiuixScrollBehavior()
            )
        },
        containerColor = Color.Transparent
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                Card {
                    SuperArrow(
                        title = "主题",
                        summary = "主题相关设置",
                        titleColor = BasicComponentDefaults.titleColor(),
                        summaryColor = BasicComponentDefaults.summaryColor(),
                        onClick = { navController.navigate("theme") }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                Card {
                    SuperArrow(
                        title = "关于",
                        summary = "版本信息与开源许可",
                        titleColor = BasicComponentDefaults.titleColor(),
                        summaryColor = BasicComponentDefaults.summaryColor(),
                        onClick = { navController.navigate("about") }
                    )
                }
            }

        }
    }

}

