package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun SessBottomBar(
    sessionState: SessionState,
    settings: AppSettings,
    credentials: UserCredentials,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onRefresh: () -> Unit,
    onResetZoom: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onOpenShortcuts: () -> Unit,
    onBypassCaptcha: () -> Unit,
    onOpenCredentials: () -> Unit,
    onOpenSessionInfo: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleDesktop: () -> Unit,
    onOpenDebugConsole: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showToolsSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 10.dp,
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Back
            IconButton(
                onClick = onBack,
                enabled = sessionState.canGoBack,
                modifier = Modifier.testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت به صفحه قبل",
                    tint = if (sessionState.canGoBack) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                )
            }

            // 2. Forward
            IconButton(
                onClick = onForward,
                enabled = sessionState.canGoForward,
                modifier = Modifier.testTag("nav_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "صفحه بعد",
                    tint = if (sessionState.canGoForward) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                )
            }

            // 3. Home (sess.shirazu.ac.ir)
            IconButton(
                onClick = onHome,
                modifier = Modifier.testTag("nav_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "صفحه نخست سس",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 4. Refresh Page
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.testTag("nav_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "بارگذاری مجدد",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 5. Shortcuts / Student Portals
            IconButton(
                onClick = onOpenShortcuts,
                modifier = Modifier.testTag("nav_shortcuts_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "پرتال‌های پرکاربرد سس",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // 6. Max Zoom-Out / Overview Fit
            IconButton(
                onClick = onResetZoom,
                modifier = Modifier.testTag("nav_zoom_overview_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "نمای کامل و حداکثر زوم‌اوت",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 7. Tools / Options Menu (Transferred from Top Bar)
            IconButton(
                onClick = { showToolsSheet = true },
                modifier = Modifier.testTag("nav_tools_menu_button")
            ) {
                if (sessionState.isCaptchaDetected) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = ShirazuError) {
                                Text("!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "ابزارها و منوی سس",
                            tint = ShirazuError
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "ابزارها و منوی سس",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Comprehensive Bottom Sheet with all actions migrated from Top Bar
    if (showToolsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showToolsSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ابزارها و امکانات Sess Plus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Session Status Indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (sessionState.isCaptchaDetected) ShirazuError.copy(alpha = 0.15f) else ShirazuSuccess.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (sessionState.isCaptchaDetected) ShirazuError else ShirazuSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (sessionState.isCaptchaDetected) "کپچا فعال" else "نشست پایدار",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sessionState.isCaptchaDetected) ShirazuError else ShirazuSuccess
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Captcha Urgent Bypass Card (if detected)
                if (sessionState.isCaptchaDetected) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ShirazuError.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ShirazuError,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "کد امنیتی شناسایی شد",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ShirazuError
                                    )
                                    Text(
                                        text = "ایجاد نشست تازه بدون کپچا",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    showToolsSheet = false
                                    onBypassCaptcha()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ShirazuError),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("رفع فوری", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Zoom Control Card (Directly addresses the user's zoom requirement)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تنظیم زاویه دید و زوم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            // Max Zoom Out Button (Featured)
                            Button(
                                onClick = {
                                    showToolsSheet = false
                                    onResetZoom()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOutMap,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حداکثر زوم‌اوت (نمای کامل)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onZoomIn,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بزرگ‌نمایی +", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onZoomOut,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("کوچک‌نمایی -", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Debug Console Item
                ToolsSheetItem(
                    icon = Icons.Default.BugReport,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "کنسول دیباگ و رفتارشناسی سس",
                    subtitle = "مشاهده زنده رویدادها، کلیک‌ها و کپی گزارش برای ارسال",
                    onClick = {
                        showToolsSheet = false
                        onOpenDebugConsole()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Auto-login Credentials Item
                ToolsSheetItem(
                    icon = if (credentials.isSaved) Icons.Default.Lock else Icons.Default.LockOpen,
                    iconTint = if (credentials.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "ورود خودکار به سس",
                    subtitle = if (credentials.isSaved) "شماره دانشجویی: ${credentials.username}" else "ثبت اطلاعات دانشجو و کلمه عبور",
                    onClick = {
                        showToolsSheet = false
                        onOpenCredentials()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Dark Mode Reading
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDarkMode() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("حالت مطالعه شب", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("رنگ‌های بهینه برای کاهش خستگی چشم", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = settings.isDarkModeReading,
                        onCheckedChange = { onToggleDarkMode() }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Desktop Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDesktop() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DesktopWindows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("نمایش دسکتاپ کامل", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("ارسال هدر سیستم‌های رومیزی", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = settings.isDesktopMode,
                        onCheckedChange = { onToggleDesktop() }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Session Security Info
                ToolsSheetItem(
                    icon = Icons.Default.Security,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "امنیت و پایداری نشست",
                    subtitle = "تپش دوره‌ای برای جلوگیری از خروج ناگهانی",
                    onClick = {
                        showToolsSheet = false
                        onOpenSessionInfo()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Full Settings
                ToolsSheetItem(
                    icon = Icons.Default.Settings,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    title = "تنظیمات کلی سامانه",
                    subtitle = "شخصی‌سازی زمان پایداری، حافظه کش و کوکی‌ها",
                    onClick = {
                        showToolsSheet = false
                        onOpenSettings()
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun ToolsSheetItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
