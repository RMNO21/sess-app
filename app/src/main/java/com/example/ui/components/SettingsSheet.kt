package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.ui.theme.ShirazuError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onClearAllData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "تنظیمات پیشرفته برنامه سس",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mobile Layout Optimization Switch
            SettingsSwitchItem(
                title = "بهینه‌سازی لمسی و موبایلی سس",
                subtitle = "تنظیم عرض جداول، خوانایی فونت فارسی، بزرگتر شدن دکمه‌های فرم",
                icon = Icons.Default.PhoneAndroid,
                checked = settings.isMobileOptimizationEnabled,
                onCheckedChange = {
                    onUpdateSettings(settings.copy(isMobileOptimizationEnabled = it))
                },
                testTag = "setting_mobile_opt_toggle"
            )

            // Desktop Mode Switch
            SettingsSwitchItem(
                title = "حالت دسکتاپ (نمای رایانه)",
                subtitle = "ارسال شناسه مرورگر دسکتاپ به سرور",
                icon = Icons.Default.DesktopWindows,
                checked = settings.isDesktopMode,
                onCheckedChange = {
                    onUpdateSettings(settings.copy(isDesktopMode = it))
                },
                testTag = "setting_desktop_mode_toggle"
            )

            // Dark Mode Filter Switch
            SettingsSwitchItem(
                title = "حالت شب / فیلتر مطالعه",
                subtitle = "تنظیم پس‌زمینه تیره برای راحتی چشم هنگام شب",
                icon = Icons.Default.DarkMode,
                checked = settings.isDarkModeReading,
                onCheckedChange = {
                    onUpdateSettings(settings.copy(isDarkModeReading = it))
                },
                testTag = "setting_dark_mode_toggle"
            )

            // Session Keep-Alive Switch
            SettingsSwitchItem(
                title = "سیستم خودکار حفظ نشست (Keep-Alive)",
                subtitle = "ارسال متناوب پالس برای جلوگیری از قطع ناگهانی و خروج از حساب",
                icon = Icons.Default.Favorite,
                checked = settings.isKeepAliveEnabled,
                onCheckedChange = {
                    onUpdateSettings(settings.copy(isKeepAliveEnabled = it))
                },
                testTag = "setting_keepalive_toggle"
            )

            // Heartbeat Interval Selector
            if (settings.isKeepAliveEnabled) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "فاصله ارسال پالس تمدید نشست:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 5).forEach { minutes ->
                            FilterChip(
                                selected = settings.keepAliveIntervalMinutes == minutes,
                                onClick = {
                                    onUpdateSettings(settings.copy(keepAliveIntervalMinutes = minutes))
                                },
                                label = { Text("$minutes دقیقه") },
                                modifier = Modifier.testTag("interval_${minutes}m")
                            )
                        }
                    }
                }
            }

            // Auto-Bypass Captcha Switch
            SettingsSwitchItem(
                title = "رفع خودکار کپچا به محض مشاهده",
                subtitle = "پاکسازی خودکار کوکی‌ها و بارگذاری فرم تمیز بدون کپچا",
                icon = Icons.Default.Security,
                checked = settings.autoBypassCaptchaOnDetect,
                onCheckedChange = {
                    onUpdateSettings(settings.copy(autoBypassCaptchaOnDetect = it))
                },
                testTag = "setting_auto_captcha_toggle"
            )

            // Text Zoom Controls
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(
                    text = "بزرگ‌نمایی متن صفحات: ${settings.textZoomPercent}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (settings.textZoomPercent > 70) {
                                onUpdateSettings(settings.copy(textZoomPercent = settings.textZoomPercent - 10))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("کوچک‌تر")
                    }
                    OutlinedButton(
                        onClick = {
                            onUpdateSettings(settings.copy(textZoomPercent = 100))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("پیش‌فرض (۱۰۰٪)")
                    }
                    OutlinedButton(
                        onClick = {
                            if (settings.textZoomPercent < 180) {
                                onUpdateSettings(settings.copy(textZoomPercent = settings.textZoomPercent + 10))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بزرگ‌تر")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Danger Zone: Clear all cookies and cache
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ShirazuError.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "پاکسازی کلی نشست و کوکی‌ها",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ShirazuError
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تمام سشن‌ها، کوکی‌های لاگین قبلی و حافظه موقت پاک می‌شوند. در صورت بروز هرگونه تداخل یا اجبار به کپچا، این گزینه مشکل را برطرف می‌کند.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onClearAllData()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShirazuError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_all_cookies_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("پاکسازی کامل کوکی‌ها و بازنشانی نشست")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 6.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
