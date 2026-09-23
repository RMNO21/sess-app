package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.SessionState
import com.example.model.UserCredentials
import com.example.ui.theme.ShirazuError
import com.example.ui.theme.ShirazuPrimary
import com.example.ui.theme.ShirazuSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessTopAppBar(
    sessionState: SessionState,
    settings: AppSettings,
    credentials: UserCredentials,
    logCount: Int,
    onRefresh: () -> Unit,
    onResetZoom: () -> Unit,
    onOpenDebugConsole: () -> Unit,
    onBypassCaptcha: () -> Unit,
    onOpenSessionInfo: () -> Unit,
    onOpenCredentials: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleDesktop: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Debug Console Button
                Surface(
                    onClick = onOpenDebugConsole,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.testTag("open_debug_console_topbar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "کنسول دیباگ",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "دیباگ ($logCount)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Center: Title & Domain
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "سامانه سس دانشگاه شیراز",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (sessionState.isCaptchaDetected) ShirazuError
                                    else ShirazuSuccess
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (sessionState.isCaptchaDetected) "کپچا شناسایی شد" else "sess.shirazu.ac.ir",
                            fontSize = 10.sp,
                            color = if (sessionState.isCaptchaDetected) ShirazuError else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right side: Quick Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onResetZoom,
                        modifier = Modifier.size(36.dp).testTag("topbar_reset_zoom_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOutMap,
                            contentDescription = "نمای کامل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(36.dp).testTag("topbar_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "بارگذاری مجدد",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp).testTag("topbar_menu_button")
                        ) {
                            if (sessionState.isCaptchaDetected) {
                                BadgedBox(badge = { Badge(containerColor = ShirazuError) { Text("!") } }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "گزینه‌ها",
                                        tint = ShirazuError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "گزینه‌ها",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("کنسول دیباگ و گزارش رویدادها") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenDebugConsole()
                                }
                            )

                            if (sessionState.isCaptchaDetected) {
                                DropdownMenuItem(
                                    text = { Text("رفع فوری کپچا و نشست نو", color = ShirazuError, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ShirazuError)
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onBypassCaptcha()
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("اطلاعات ورود خودکار") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenCredentials()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (settings.isDarkModeReading) "غیرفعال‌سازی حالت شب" else "حالت مطالعه شب") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.DarkMode, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleDarkMode()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (settings.isDesktopMode) "نمایش موبایل" else "نمایش نسخه دسکتاپ") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.DesktopWindows, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleDesktop()
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("بزرگ‌نمایی متن (+)") },
                                leadingIcon = { Icon(Icons.Default.ZoomIn, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onZoomIn()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("کوچک‌نمایی متن (-)") },
                                leadingIcon = { Icon(Icons.Default.ZoomOut, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onZoomOut()
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("امنیت و پایداری نشست") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSessionInfo()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("تنظیمات") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }
                }
            }

            // Slim progress indicator on loading
            if (sessionState.isLoading && sessionState.pageProgress in 1..99) {
                LinearProgressIndicator(
                    progress = { sessionState.pageProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
