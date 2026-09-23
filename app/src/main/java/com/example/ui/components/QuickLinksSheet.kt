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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CustomShortcut
import com.example.model.QuickLink

data class SessSubItem(
    val title: String,
    val script: String
)

data class SessCategory(
    val title: String,
    val iconName: String,
    val items: List<SessSubItem>
)

val SESS_CATEGORIES = listOf(
    SessCategory(
        title = "اطلاعات کاربری",
        iconName = "person",
        items = listOf(
            SessSubItem("تغییر کلمه رمز", "PerformStd('Cgp')"),
            SessSubItem("اطلاعات پایه", "Perform('SignUp')"),
            SessSubItem("اطلاعات افزوده", "PerformStd('Ovs')"),
            SessSubItem("ارسال مدارک", "PerformStd('Sdc')"),
            SessSubItem("مشخصات دانشجویی", "Perform('BasicInfo')"),
            SessSubItem("خطاهای سیستم", "PerformStd('ErrRep')"),
            SessSubItem("پرونده دیجیتال", "PerformStd('SDGF')"),
            SessSubItem("اطلاعات کنکور", "PerformStd('Ssd')"),
            SessSubItem("آیین نامه‌ها", "PerformStd('Reg')"),
            SessSubItem("تابلو احکام جدید", "PerformStd('Amt')"),
            SessSubItem("دریافت رمز اولیه", "PerformStd('KeyG')"),
            SessSubItem("تاریخچه ورود", "PerformStd('Vlg')"),
            SessSubItem("انتخاب کاربر پیش‌فرض SSO", "PerformStd('SDEF')")
        )
    ),
    SessCategory(
        title = "پیام‌ها",
        iconName = "email",
        items = listOf(
            SessSubItem("پیام‌ها", "PerformStd('Msg')"),
            SessSubItem("پیام به استاد مشاور", "Perform('Msg2Tch')"),
            SessSubItem("پیام به کارشناس بخش", "Perform('Msg2Exp')")
        )
    ),
    SessCategory(
        title = "آموزشی",
        iconName = "school",
        items = listOf(
            SessSubItem("برنامه درسی", "Perform('Major')"),
            SessSubItem("دروس جبرانی", "Perform('Compensate')"),
            SessSubItem("لیست دروس گرفته", "Perform('Som')"),
            SessSubItem("نمودار پیشرفت تحصیلی", "PerformStd('StdProgress')"),
            SessSubItem("خلاصه کارنامه", "PerformStd('Sum')"),
            SessSubItem("چک‌لیست ثبت نام مقدماتی", "PerformStd('Prc')"),
            SessSubItem("چک‌لیست انتخاب واحد", "PerformStd('Rcf')"),
            SessSubItem("عملیات‌های ثبت نام", "Perform('RegLog')"),
            SessSubItem("فارغ‌التحصیلی", "PerformStd('Sgr')"),
            SessSubItem("برنامه کلاسی نیمسال", "PerformStd('Pcl')"),
            SessSubItem("تقویم آموزشی", "PerformStd('Ssr')"),
            SessSubItem("سوابق تحصیلی انتقالی", "PerformStd('See')"),
            SessSubItem("آزمون‌های معافی", "PerformStd('Exl')"),
            SessSubItem("امور دستیار استاد", "Perform('ExamTA')"),
            SessSubItem("جلسات مشاوره", "PerformStd('StdCons')")
        )
    ),
    SessCategory(
        title = "ارزیابی",
        iconName = "check",
        items = listOf(
            SessSubItem("تکمیل فرم‌های ارزیابی", "PerformStd('ActiveEvl')")
        )
    ),
    SessCategory(
        title = "امور دانشجویی",
        iconName = "hotel",
        items = listOf(
            SessSubItem("خوابگاه", "PerformStd('Dst')"),
            SessSubItem("فرم هم‌اتاقی", "Perform('DormAgent')"),
            SessSubItem("خوابگاه ورودی‌های جدید", "Perform('DormitoryZero')"),
            SessSubItem("درخواست وام", "PerformStd('Erl')"),
            SessSubItem("انتخابات دانشجویی", "PerformStd('Evt')"),
            SessSubItem("خرید ژتون و رفاهی", "Perform('SfxChip')"),
            SessSubItem("ثبت نام مراسم فارغ‌التحصیلی", "PerformStd('GRDSTD')"),
            SessSubItem("شبکه آزمایشگاهی دانشگاه", "PerformStd('LabsView')"),
            SessSubItem("درخواست‌های اسکان متفرقه", "PerformStd('NewRoomerReqs')"),
            SessSubItem("درخواست‌های نوبت‌دهی آزمایشگاه", "Perform('LabServReqs')")
        )
    ),
    SessCategory(
        title = "امور مالی",
        iconName = "payment",
        items = listOf(
            SessSubItem("پرداخت شهریه اینترنتی", "Perform('IntPy')"),
            SessSubItem("لیست پرداخت‌ها", "PerformStd('Scr')"),
            SessSubItem("پرداخت‌های اینترنتی", "PerformStd('Psp')"),
            SessSubItem("چک‌های تقسیطی", "PerformStd('Psc')"),
            SessSubItem("چک‌لیست مالی نیمسال", "PerformStd('Sfc')"),
            SessSubItem("جدول شهریه", "Perform('AccTable')"),
            SessSubItem("شهریه", "Perform('Pst')"),
            SessSubItem("شهریه رایگان", "PerformStd('Frt')"),
            SessSubItem("بدهی‌های موضوعی", "PerformStd('StdDebPay')"),
            SessSubItem("حق‌التدریس دستیار آموزشی", "PerformStd('HSC')")
        )
    ),
    SessCategory(
        title = "فرایندها",
        iconName = "sync",
        items = listOf(
            SessSubItem("فرآیند ثبت نام", "Perform('Spr')"),
            SessSubItem("منابع و برنامه‌ها", "PerformStd('Rss')"),
            SessSubItem("فرآیندهای دانشجویی", "PerformStd('Ssi')"),
            SessSubItem("نوبت‌های مراجعه", "PerformStd('Ats')"),
            SessSubItem("فرآیند ثبت نام جدید", "Perform('RegStdProc')")
        )
    ),
    SessCategory(
        title = "امور فرهنگی",
        iconName = "culture",
        items = listOf(
            SessSubItem("امور فرهنگی", "PerformStd('CLE')")
        )
    ),
    SessCategory(
        title = "امور واحدهای مجازی",
        iconName = "computer",
        items = listOf(
            SessSubItem("کتابخانه دیجیتال", "PerformStd('DgtL')"),
            SessSubItem("گفتگو با کارشناس بخش", "if (window.Connect2EduExpert) Connect2EduExpert();"),
            SessSubItem("گفتگو با کارشناس حسابداری", "if (window.Connect2CalExpert) Connect2CalExpert();")
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLinksSheet(
    links: List<QuickLink>,
    customShortcuts: List<CustomShortcut> = emptyList(),
    onSelectLink: (QuickLink) -> Unit,
    onSelectCustomShortcut: (CustomShortcut) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "منو و میان‌برهای سامانه سس شیراز",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "دسترسی مستقیم و بدون دردسر به تمام امکانات پرتال",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "منوی کامل ۹ گانه",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "میان‌برهای سریع",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // SESS 9 Categories Tab with Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sess_menu_search_input"),
                    placeholder = {
                        Text("جستجو در تمام بخش‌های سس (کارنامه، رمز، خوابگاه...)", fontSize = 12.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "پاک کردن", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchQuery.isNotBlank()) {
                        val filteredItems = mutableListOf<Pair<String, SessSubItem>>()
                        SESS_CATEGORIES.forEach { cat ->
                            cat.items.forEach { item ->
                                if (item.title.contains(searchQuery, ignoreCase = true) ||
                                    cat.title.contains(searchQuery, ignoreCase = true)
                                ) {
                                    filteredItems.add(cat.title to item)
                                }
                            }
                        }

                        if (filteredItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "موردی یافت نشد",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            items(filteredItems) { (catTitle, item) ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val safeScript = "if (window.runSessCmd) { runSessCmd('${item.script.replace("'", "\\'")}'); } else { ${item.script} }"
                                            onSelectLink(
                                                QuickLink(
                                                    title = item.title,
                                                    description = catTitle,
                                                    url = "https://sess.shirazu.ac.ir",
                                                    category = catTitle,
                                                    iconName = "school",
                                                    actionScript = safeScript
                                                )
                                            )
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = catTitle,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowLeft,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Show all 9 expandable categories
                        items(SESS_CATEGORIES) { category ->
                            val isExpanded = expandedMap[category.title] ?: false

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                expandedMap[category.title] = !isExpanded
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = getIconForCategory(category.iconName),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = category.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text(
                                                text = "${category.items.size} بخش",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "بستن" else "باز کردن",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp)
                                                .padding(bottom = 8.dp)
                                        ) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            )
                                            category.items.forEach { item ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            val safeScript = "if (window.runSessCmd) { runSessCmd('${item.script.replace("'", "\\'")}'); } else { ${item.script} }"
                                                            onSelectLink(
                                                                QuickLink(
                                                                    title = item.title,
                                                                    description = category.title,
                                                                    url = "https://sess.shirazu.ac.ir",
                                                                    category = category.title,
                                                                    iconName = category.iconName,
                                                                    actionScript = safeScript
                                                                )
                                                            )
                                                            onDismiss()
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = item.title,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.KeyboardArrowLeft,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Quick Links & Custom Shortcuts Tab
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    if (customShortcuts.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "میانبرهای اختصاصی شما (${customShortcuts.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                        }

                        items(customShortcuts, key = { "custom_${it.id}" }) { custom ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectCustomShortcut(custom)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = custom.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "میانبر ذخیره شده",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "بخش‌های دانشگاهی",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }
                    }

                    items(links) { link ->
                        QuickLinkCard(
                            link = link,
                            onClick = {
                                onSelectLink(link)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickLinkCard(
    link: QuickLink,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("quicklink_${link.iconName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getIconForCategory(link.iconName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = link.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun getIconForCategory(name: String): ImageVector {
    return when (name) {
        "person" -> Icons.Default.Person
        "email" -> Icons.Default.Email
        "school" -> Icons.Default.School
        "check" -> Icons.Default.CheckCircle
        "hotel" -> Icons.Default.Hotel
        "payment" -> Icons.Default.Payment
        "sync" -> Icons.Default.Sync
        "culture" -> Icons.Default.AccountBalance
        "computer" -> Icons.Default.Computer
        "home" -> Icons.Default.Home
        "grade" -> Icons.Default.Grade
        "schedule" -> Icons.Default.Schedule
        "restaurant" -> Icons.Default.Restaurant
        else -> Icons.Default.School
    }
}
