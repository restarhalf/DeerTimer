package me.restarhalf.deer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.Session
import me.restarhalf.deer.data.SessionDraft
import me.restarhalf.deer.data.SessionRepository
import me.restarhalf.deer.ui.components.TwoTextButtonsRow
import me.restarhalf.deer.ui.dialogs.DetailsDialog
import me.restarhalf.deer.ui.util.formatTime
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.extra.WindowListPopup
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val sessions = remember { mutableStateListOf<Session>() }
    val scope = rememberCoroutineScope()

    var editSession by remember { mutableStateOf<Session?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    var draft by remember { mutableStateOf(SessionDraft()) }

    val showMenu = remember { mutableStateOf(false) }
    val showClearDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showViewDialog = remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<Session?>(null) }
    var sessionToView by remember { mutableStateOf<Session?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { os ->
                OutputStreamWriter(os).use { writer ->
                    writer.write(SessionRepository.encodeSessions(sessions))
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)
                ?.bufferedReader()
                ?.use { reader ->
                    val jsonStr = reader.readText()
                    val imported = SessionRepository.decodeSessions(jsonStr)
                    sessions.clear()
                    sessions.addAll(imported)
                    scope.launch {
                        SessionRepository.saveSessionsJson(context, jsonStr)
                    }
                }
        }
    }


    LaunchedEffect(Unit) {
        val loaded = SessionRepository.loadSessions(context)
        sessions.clear()
        sessions.addAll(loaded)
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "历史记录",
                color = Color.Transparent,
                scrollBehavior = MiuixScrollBehavior(),
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu.value = true },
                            holdDownState = showMenu.value
                        ) {
                            Icon(
                                imageVector = MiuixIcons.More,
                                contentDescription = "更多",
                                tint = MiuixTheme.colorScheme.onBackground
                            )
                        }

                        WindowListPopup(
                            show = showMenu,
                            alignment = PopupPositionProvider.Align.BottomEnd,
                            onDismissRequest = { showMenu.value = false }
                        ) {
                            val menuItems = listOf("导出数据", "导入数据", "清除全部记录")
                            ListPopupColumn {
                                menuItems.forEachIndexed { index, text ->
                                    DropdownImpl(
                                        text = text,
                                        optionSize = menuItems.size,
                                        isSelected = false,
                                        onSelectedIndexChange = { selectedIdx ->
                                            showMenu.value = false
                                            when (selectedIdx) {
                                                0 -> exportLauncher.launch("DeerTimer_export.json")
                                                1 -> importLauncher.launch(arrayOf("application/json"))
                                                2 -> showClearDialog.value = true
                                            }
                                        },
                                        index = index
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "(。・ω・。)",
                            style = MiuixTheme.textStyles.title2
                        )
                        Text(
                            text = "暂无历史记录哦！",
                            style = MiuixTheme.textStyles.body2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    overscrollEffect = null
                ) {
                    items(sessions) { session ->
                        Card {
                            BasicComponent(
                                title = "时间: " + session.timestamp.format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                ),
                                summary = buildString {
                                    append("持续: ")
                                    append(formatTime(session.duration))
                                    if (session.remark.isNotEmpty()) {
                                        append("\n备注: ")
                                        append(session.remark)
                                    }
                                },
                                onClick = {
                                    sessionToView = session
                                    showViewDialog.value = true
                                },
                                endActions = {
                                    IconButton(
                                        onClick = {
                                            sessionToDelete = session
                                            showDeleteDialog.value = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Delete,
                                            contentDescription = "删除",
                                            tint = MiuixTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
            WindowDialog(
                title = "删除记录",
                summary = "确认删除此记录？",
                show = showDeleteDialog,
                onDismissRequest = {
                    showDeleteDialog.value = false
                    sessionToDelete = null
                }
            ) {
                TwoTextButtonsRow(
                    leftText = "取消",
                    onLeftClick = {
                        showDeleteDialog.value = false
                        sessionToDelete = null
                    },
                    rightText = "确认",
                    onRightClick = {
                        val target = sessionToDelete
                        if (target != null) {
                            sessions.remove(target)
                            scope.launch { SessionRepository.saveSessions(context, sessions) }
                        }
                        showDeleteDialog.value = false
                        sessionToDelete = null
                    },
                )
            }

            WindowDialog(
                title = "清除全部记录",
                summary = "确认要清除所有历史记录吗？此操作不可撤销。",
                show = showClearDialog,
                onDismissRequest = { showClearDialog.value = false }
            ) {
                TwoTextButtonsRow(
                    leftText = "取消",
                    onLeftClick = { showClearDialog.value = false },
                    rightText = "删除",
                    onRightClick = {
                        sessions.clear()
                        scope.launch {
                            SessionRepository.clearSessions(context)
                        }
                        showClearDialog.value = false
                    },
                )
            }

            sessionToView?.let { s ->
                WindowDialog(
                    title = "会话详情",
                    show = showViewDialog,
                    onDismissRequest = {
                        showViewDialog.value = false
                        sessionToView = null
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        Text(
                            "开始时间：" + s.timestamp.format(pat),
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "持续时长：" + formatTime(s.duration),
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "备注：" + s.remark.ifEmpty { "无" },
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "地点：" + s.location.ifEmpty { "无" },
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "是否观看小电影：" + if (s.watchedMovie) "是" else "否",
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "发射：" + if (s.climax) "是" else "否",
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "道具：" + s.props.ifEmpty { "无" },
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "评分：" + "%.1f".format(s.rating) + " / 5.0",
                            style = MiuixTheme.textStyles.body1
                        )
                        Text("心情：" + s.mood.ifEmpty { "无" }, style = MiuixTheme.textStyles.body1)

                        Spacer(modifier = Modifier.height(12.dp))

                        TwoTextButtonsRow(
                            leftText = "关闭",
                            onLeftClick = {
                                showViewDialog.value = false
                                sessionToView = null
                            },
                            rightText = "编辑",
                            onRightClick = {
                                editSession = s
                                isEditing = true
                                draft = SessionDraft.fromSession(s)
                                showDetailsDialog = true
                                showViewDialog.value = false
                                sessionToView = null
                            },
                        )
                    }
                }
            }

            DetailsDialog(
                show = showDetailsDialog,
                draft = draft,
                onDraftChange = { draft = it },
                onConfirm = {
                    if (isEditing && editSession != null) {
                        val idx = sessions.indexOf(editSession!!)
                        if (idx >= 0) {
                            sessions[idx] = draft.applyTo(editSession!!)
                        }
                    }

                    scope.launch {
                        SessionRepository.saveSessions(context, sessions)
                    }

                    draft = SessionDraft()
                    showDetailsDialog = false
                    isEditing = false
                    editSession = null
                },
                onDismiss = {
                    showDetailsDialog = false
                    isEditing = false
                    editSession = null
                }
            )
        }
    }
}
