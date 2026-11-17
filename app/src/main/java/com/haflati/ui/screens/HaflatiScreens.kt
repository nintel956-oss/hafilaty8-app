package com.haflati.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.haflati.data.MockHaflatiRepository
import com.haflati.model.*
import com.haflati.ui.theme.HaflatiPurple
import com.haflati.ui.theme.HaflatiSoftGold
import com.haflati.viewmodel.HaflatiUiState
import java.util.Locale

fun formatPriceInCurrency(amountSar: Double, currency: CurrencyType, rate: Double): String {
    val converted = amountSar * rate
    return if (currency == CurrencyType.USD) {
        String.format(Locale.US, "$%.2f", converted)
    } else {
        "${converted.toInt()} ${currency.symbolAr}"
    }
}

/**
 * شعار تطبيق حفلتي الرسمي (يدعم المظهر الفاتح والداكن)
 */
@Composable
fun HaflatiLogo(isDarkSurface: Boolean, showTagline: Boolean = false) {
    val textColor = if (isDarkSurface) Color.White else HaflatiPurple
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HaflatiPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "شعار حفلتي",
                    tint = HaflatiSoftGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "حفلتي",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
        }
        if (showTagline) {
            Text(
                text = "تنسيق مناسبتك من مكان واحد",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * الشريط العلوي: شعار حفلتي + الرسالة الترحيبية فقط (دون مبدل عملة أو تبديل حساب)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaflatiTopAppBar(
    userSession: UserSession,
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "رجوع"
                    )
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HaflatiLogo(isDarkSurface = false)
                Text(
                    text = "| أهلاً بك يا ${userSession.name} · حساب ${userSession.role.titleAr}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}

/**
 * شاشة تسجيل الدخول للجوال: اختيار عميل أو تاجر وإدخال الاسم فقط
 */
@Composable
fun LoginScreen(onLogin: (String, UserRole) -> Unit) {
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var name by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            HaflatiLogo(isDarkSurface = false, showTagline = true)
            Spacer(Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("تسجيل الدخول", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "اختر نوع الحساب ثم أدخل اسمك فقط للمتابعة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleChoiceCard(
                            title = "عميل",
                            subtitle = "تصفح وتسوق الحفلة",
                            selected = selectedRole == UserRole.CUSTOMER,
                            icon = Icons.Rounded.Person,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedRole = UserRole.CUSTOMER }
                        )
                        RoleChoiceCard(
                            title = "تاجر",
                            subtitle = "نشر الصور والمنشورات",
                            selected = selectedRole == UserRole.MERCHANT,
                            icon = Icons.Rounded.Store,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedRole = UserRole.MERCHANT }
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم فقط") },
                        placeholder = {
                            Text(
                                if (selectedRole == UserRole.CUSTOMER) "مثال: محمد الحارثي"
                                else "مثال: قصر اللؤلؤة الملكي"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { if (name.isNotBlank()) onLogin(name, selectedRole) },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            if (selectedRole == UserRole.CUSTOMER) "دخول كعميل" else "دخول كتاجر",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) HaflatiPurple else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) HaflatiPurple else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * الشريط الثابت أسفل الشاشة: يعرض "إجمالي التكلفة" ويتحدث تلقائياً ويفتح السلة عند الضغط
 */
@Composable
fun PersistentTotalCostBar(
    totalCost: Double,
    itemCount: Int,
    isOverBudget: Boolean,
    currency: CurrencyType,
    exchangeRate: Double,
    onOpenCart: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenCart() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "إجمالي التكلفة ($itemCount خدمة)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatPriceInCurrency(totalCost, currency, exchangeRate),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else HaflatiPurple,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onOpenCart,
                colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Rounded.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("عرض السلة")
            }
        }
    }
}

/**
 * 1. الشاشة الرئيسية للجوال
 */
@Composable
fun HomeScreen(
    uiState: HaflatiUiState,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onStartWizard: () -> Unit,
    onOpenMerchantPublish: () -> Unit,
    onEventClick: (EventType) -> Unit,
    onAddPackage: (ReadyPackage) -> Unit,
    onServiceClick: (ServiceItem) -> Unit
) {
    val isMerchant = uiState.userSession?.role == UserRole.MERCHANT

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // بطاقة الترحيب والبحث السريع
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "تنظيم شامل من هاتفك · تعاقد موحّد مع طرف واحد",
                        style = MaterialTheme.typography.labelLarge,
                        color = HaflatiSoftGold
                    )
                    Text(
                        "رتّب حفلتك كاملة في مكان واحد بدون تشتّت",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("ابحث عن قاعة، كوشة، مصورة...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onSearchSubmit,
                            colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("بحث")
                        }
                    }

                    if (!isMerchant) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "تجهيزات حفلتك: ${uiState.completedCategoriesCount} / ${ServiceCategory.entries.size} أقسام",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "الميزانية: ${formatPriceInCurrency(uiState.wizardConfig.budget, uiState.currency, uiState.currentRate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isOverBudget) MaterialTheme.colorScheme.error else HaflatiPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (uiState.completedCategoriesCount / 9f).coerceAtLeast(0.06f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = HaflatiPurple
                        )
                        Button(
                            onClick = onStartWizard,
                            colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("ابدأ حفلتك — معالج التنسيق الذكي")
                        }
                    } else {
                        HorizontalDivider()
                        Text(
                            "أنت في حساب التاجر: يمكنك إضافة الصور والمنشورات وإدارة الخدمات ولا يظهر لك التسوق.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenMerchantPublish,
                            colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("نشر صورة أو منشور جديد")
                        }
                    }
                }
            }
        }

        // منشورات وصور التجار
        if (uiState.merchantPosts.isNotEmpty()) {
            item {
                Text("أحدث منشورات وصور التجار", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.merchantPosts) { post ->
                        Card(
                            modifier = Modifier.width(260.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column {
                                ServiceVisualBanner(
                                    title = post.title,
                                    badge = "${post.city} · ${post.createdAt}",
                                    heightDp = 110
                                )
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        post.authorName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = HaflatiSoftGold
                                    )
                                    Text(post.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        post.caption,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // أنواع المناسبات
        item {
            Text("اختر نوع مناسبتك", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(EventType.entries) { eventType ->
                    Card(
                        modifier = Modifier
                            .width(175.dp)
                            .clickable { onEventClick(eventType) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                eventType.titleAr,
                                style = MaterialTheme.typography.titleMedium,
                                color = HaflatiPurple,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                eventType.subtitleAr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // باقات جاهزة مخفضة
        item {
            Text("باقات جاهزة بأسعار مخفضة", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.readyPackages.forEach { pkg ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(pkg.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                pkg.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    formatPriceInCurrency(pkg.discountedPrice, uiState.currency, uiState.currentRate),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = HaflatiPurple
                                )
                                if (!isMerchant) {
                                    Button(
                                        onClick = { onAddPackage(pkg) },
                                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("أضف الباقة للسلة")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // عروض مميزة
        item {
            Text("عروض مميزة", style = MaterialTheme.typography.titleLarge)
        }
        items(uiState.allServices.filter { it.isFeaturedOffer }) { service ->
            ServiceMobileCard(
                service = service,
                currency = uiState.currency,
                rate = uiState.currentRate,
                isFavorite = service.id in uiState.favoriteIds,
                isCompared = service.id in uiState.compareIds,
                isMerchant = isMerchant,
                onOpen = { onServiceClick(service) },
                onToggleFavorite = {},
                onToggleCompare = {}
            )
        }
    }
}

/**
 * 2. شاشة أقسام الخدمات التسعة
 */
@Composable
fun CategoriesScreen(
    selectedEventType: EventType?,
    onSelectCategory: (ServiceCategory) -> Unit,
    onShowAllServices: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("أقسام الخدمات", style = MaterialTheme.typography.headlineMedium)
                    if (selectedEventType != null) {
                        Text(
                            "المقترحة لمناسبة ${selectedEventType.titleAr}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HaflatiSoftGold
                        )
                    }
                }
                TextButton(onClick = onShowAllServices) {
                    Text("عرض الكل")
                }
            }
        }

        items(ServiceCategory.entries) { category ->
            val isRecommended = selectedEventType?.recommendedCategories?.contains(category) == true
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCategory(category) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            category.titleAr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            category.shortDescAr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isRecommended) {
                            Text(
                                "موصى به لمناسبتك",
                                style = MaterialTheme.typography.labelLarge,
                                color = HaflatiSoftGold
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = HaflatiPurple
                    )
                }
            }
        }
    }
}

/**
 * 3. شاشة قائمة الخدمات مع الفلاتر (المدينة، السعر، السعة، التقييم) والترتيب والمقارنة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    uiState: HaflatiUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (ServiceFilterState) -> Unit,
    onSelectCategory: (ServiceCategory?) -> Unit,
    onOpenService: (ServiceItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleCompare: (String) -> Unit,
    onOpenCompareDialog: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val services = uiState.filteredServices()
    val isMerchant = uiState.userSession?.role == UserRole.MERCHANT

    Column(modifier = Modifier.fillMaxSize()) {
        // شريط البحث والفلترة السريع
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("ابحث باسم الخدمة أو المدينة...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Rounded.FilterList, contentDescription = "فلترة وترتيب")
                }
            }

            // شريط اختيار القسم
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("الكل") }
                    )
                }
                items(ServiceCategory.entries) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat.titleAr) }
                    )
                }
            }

            if (uiState.compareIds.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "المقارنة (${uiState.compareIds.size}/3 خدمات)",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Button(
                            onClick = onOpenCompareDialog,
                            enabled = uiState.compareIds.size >= 2,
                            colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple)
                        ) {
                            Text("قارن الآن")
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services, key = { it.id }) { service ->
                ServiceMobileCard(
                    service = service,
                    currency = uiState.currency,
                    rate = uiState.currentRate,
                    isFavorite = service.id in uiState.favoriteIds,
                    isCompared = service.id in uiState.compareIds,
                    isMerchant = isMerchant,
                    onOpen = { onOpenService(service) },
                    onToggleFavorite = { onToggleFavorite(service.id) },
                    onToggleCompare = { onToggleCompare(service.id) }
                )
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            var city by remember { mutableStateOf(uiState.filterState.selectedCity) }
            var maxPrice by remember { mutableFloatStateOf(uiState.filterState.maxPrice.toFloat()) }
            var minCap by remember { mutableStateOf(uiState.filterState.minCapacity) }
            var minRating by remember { mutableFloatStateOf(uiState.filterState.minRating) }
            var sort by remember { mutableStateOf(uiState.filterState.sortOption) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("تصفية وترتيب الخدمات", style = MaterialTheme.typography.titleLarge)

                Text("المدينة اليمنية", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MockHaflatiRepository.availableCities) { c ->
                        FilterChip(selected = city == c, onClick = { city = c }, label = { Text(c) })
                    }
                }

                Text(
                    "الحد الأعلى للسعر: ${formatPriceInCurrency(maxPrice.toDouble(), uiState.currency, uiState.currentRate)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    valueRange = 1000f..25000f
                )

                Text("السعة الدنيا (عدد الضيوف)", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(0, 100, 200, 300)) { cap ->
                        FilterChip(
                            selected = minCap == cap,
                            onClick = { minCap = cap },
                            label = { Text(if (cap == 0) "أي سعة" else "$cap+ ضيف") }
                        )
                    }
                }

                Text("الترتيب", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SortOption.entries) { option ->
                        FilterChip(
                            selected = sort == option,
                            onClick = { sort = option },
                            label = { Text(option.titleAr) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onFilterChange(ServiceFilterState())
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("إعادة ضبط") }
                    Button(
                        onClick = {
                            onFilterChange(
                                ServiceFilterState(
                                    selectedCity = city,
                                    maxPrice = maxPrice.toDouble(),
                                    minCapacity = minCap,
                                    minRating = minRating,
                                    sortOption = sort
                                )
                            )
                            showFilterSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        modifier = Modifier.weight(1f)
                    ) { Text("تطبيق") }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ServiceVisualBanner(
    title: String,
    badge: String?,
    heightDp: Int = 128
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(
                Brush.linearGradient(
                    listOf(HaflatiPurple, Color(0xFF2B1045))
                )
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (badge != null) {
                Text(
                    badge,
                    style = MaterialTheme.typography.labelLarge,
                    color = HaflatiSoftGold
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ServiceMobileCard(
    service: ServiceItem,
    currency: CurrencyType,
    rate: Double,
    isFavorite: Boolean,
    isCompared: Boolean,
    isMerchant: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleCompare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                ServiceVisualBanner(
                    title = service.category.titleAr,
                    badge = service.discountLabel,
                    heightDp = 115
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCompared) HaflatiSoftGold else Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = onToggleCompare) {
                            Icon(
                                Icons.Rounded.Scale,
                                contentDescription = "مقارنة",
                                tint = if (isCompared) Color.White else HaflatiPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (!isMerchant) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            IconButton(onClick = onToggleFavorite) {
                                Icon(
                                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "المفضلة",
                                    tint = if (isFavorite) Color(0xFFE11D48) else HaflatiPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "${service.city} · سعة ${service.capacity} · ★ ${service.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(service.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatPriceInCurrency(service.startingPrice, currency, rate),
                        style = MaterialTheme.typography.titleLarge,
                        color = HaflatiPurple
                    )
                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("التفاصيل")
                    }
                }
            }
        }
    }
}

/**
 * 4. تفاصيل الخدمة: صور، فيديو تعريفي، مميزات، التحقق من التاريخ، العربون، والتقييمات
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailSheet(
    service: ServiceItem,
    defaultDate: String,
    currency: CurrencyType,
    rate: Double,
    userRole: UserRole,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddToCart: (ServiceItem, String) -> Unit,
    onAddReview: (String, Float, String) -> Unit
) {
    var selectedDate by remember(service.id) { mutableStateOf(defaultDate) }
    var reviewText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableFloatStateOf(5f) }
    val isDateBooked = selectedDate in service.bookedDates

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                ServiceVisualBanner(
                    title = service.title,
                    badge = "${service.city} · ${service.category.titleAr}",
                    heightDp = 140
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatPriceInCurrency(service.startingPrice, currency, rate),
                        style = MaterialTheme.typography.headlineMedium,
                        color = HaflatiPurple
                    )
                    if (userRole == UserRole.CUSTOMER) {
                        IconButton(onClick = { onToggleFavorite(service.id) }) {
                            Icon(
                                if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "حفظ في المفضلة",
                                tint = if (isFavorite) Color(0xFFE11D48) else HaflatiPurple
                            )
                        }
                    }
                }
                Text(service.description, style = MaterialTheme.typography.bodyLarge)
            }

            // بطاقة الفيديو التعريفي
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.PlayCircle, contentDescription = null, tint = HaflatiPurple, modifier = Modifier.size(32.dp))
                        Column {
                            Text(service.videoPreviewTitle, style = MaterialTheme.typography.titleMedium)
                            Text("فيديو تعريفي (${service.videoDuration})", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // المميزات
            item {
                Text("المميزات المشمولة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                service.features.forEach { feat ->
                    Text("• $feat", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // التحقق من توفر التاريخ
            item {
                Text("اختيار والتحقق من توفر التاريخ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("التاريخ (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isDateBooked) "تنبيه: هذا التاريخ محجوز مسبقاً، يرجى اختيار يوم آخر."
                    else "التاريخ متاح للحجز الفوري.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDateBooked) MaterialTheme.colorScheme.error else Color(0xFF15803D),
                    fontWeight = FontWeight.Bold
                )
            }

            // العربون وسياسة الإلغاء
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "حجز بعربون (${service.depositPercent}%): ${formatPriceInCurrency(service.startingPrice * service.depositPercent / 100.0, currency, rate)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(service.cancellationPolicy, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // زر أضف إلى السلة (للعميل فقط)
            item {
                if (userRole == UserRole.CUSTOMER) {
                    Button(
                        onClick = {
                            onAddToCart(service, selectedDate)
                            onDismiss()
                        },
                        enabled = !isDateBooked && selectedDate.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("أضف إلى السلة")
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "أنت مسجل كـ «تاجر» — ميزة التسوق والإضافة للسلة متاحة لحساب العميل فقط.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // تقييمات وتعليقات الزبائن
            item {
                Text("تقييمات الزبائن (${service.reviewCount})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                service.reviews.forEach { rev ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${rev.authorName} · ★ ${rev.rating} · ${rev.dateLabel}", style = MaterialTheme.typography.labelLarge)
                            Text(rev.comment, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("أضف تعليقك وتقييمك") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = {
                        onAddReview(service.id, reviewRating, reviewText)
                        reviewText = ""
                    },
                    enabled = reviewText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple)
                ) {
                    Text("إرسال التقييم")
                }
            }
        }
    }
}
