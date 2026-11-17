package com.haflati.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haflati.data.MockHaflatiRepository
import com.haflati.model.*
import com.haflati.ui.theme.HaflatiPurple
import com.haflati.ui.theme.HaflatiSoftGold
import com.haflati.viewmodel.HaflatiUiState

/**
 * شاشة التاجر: إضافة صور ومنشورات، إضافة/تعديل خدمات، ومتابعة الطلبات الواردة
 */
@Composable
fun MerchantDashboardScreen(
    uiState: HaflatiUiState,
    onAddPost: (title: String, caption: String, city: String, imageUri: String) -> Unit,
    onUpsertService: (ServiceItem) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onOpenSettings: () -> Unit
) {
    var postTitle by remember { mutableStateOf("") }
    var postCaption by remember { mutableStateOf("") }
    var postCity by remember { mutableStateOf("صنعاء") }
    var selectedImageUri by remember { mutableStateOf("haflati_kosha_stage") }

    var serviceTitle by remember { mutableStateOf("") }
    var serviceCity by remember { mutableStateOf("صنعاء") }
    var servicePrice by remember { mutableStateOf("3500") }
    var serviceCap by remember { mutableStateOf("150") }
    var serviceCat by remember { mutableStateOf(ServiceCategory.HALLS) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("لوحة التاجر", style = MaterialTheme.typography.headlineMedium)
                OutlinedButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("الإعدادات")
                }
            }
        }

        // نشر صورة ومنشور جديد
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("نشر صورة ومنشور جديد للعملاء", style = MaterialTheme.typography.titleLarge)

                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("اختيار صورة من جهاز الجوال")
                    }

                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("عنوان المنشور") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = postCaption,
                        onValueChange = { postCaption = it },
                        label = { Text("تفاصيل العرض أو الصورة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(MockHaflatiRepository.availableCities.drop(1)) { c ->
                            FilterChip(selected = postCity == c, onClick = { postCity = c }, label = { Text(c) })
                        }
                    }

                    Button(
                        onClick = {
                            if (postTitle.isNotBlank() && postCaption.isNotBlank()) {
                                onAddPost(postTitle, postCaption, postCity, selectedImageUri)
                                postTitle = ""
                                postCaption = ""
                            }
                        },
                        enabled = postTitle.isNotBlank() && postCaption.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("نشر الآن")
                    }
                }
            }
        }

        // إضافة خدمة جديدة
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("إضافة خدمة جديدة للقائمة", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = serviceTitle,
                        onValueChange = { serviceTitle = it },
                        label = { Text("اسم الخدمة أو القاعة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = servicePrice,
                            onValueChange = { servicePrice = it },
                            label = { Text("السعر (ر.س)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = serviceCap,
                            onValueChange = { serviceCap = it },
                            label = { Text("السعة") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ServiceCategory.entries) { cat ->
                            FilterChip(
                                selected = serviceCat == cat,
                                onClick = { serviceCat = cat },
                                label = { Text(cat.titleAr) }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (serviceTitle.isNotBlank()) {
                                onUpsertService(
                                    ServiceItem(
                                        id = "srv-${System.currentTimeMillis()}",
                                        category = serviceCat,
                                        title = serviceTitle.trim(),
                                        city = serviceCity,
                                        startingPrice = servicePrice.toDoubleOrNull() ?: 2500.0,
                                        capacity = serviceCap.toIntOrNull() ?: 150,
                                        rating = 4.8f,
                                        reviewCount = 1,
                                        description = "خدمة مضافة من حساب التاجر ${uiState.userSession?.name ?: ""}.",
                                        features = listOf("تنسيق شامل", "التزام بالمواعيد"),
                                        imageResNames = listOf("haflati_hero_ballroom"),
                                        videoPreviewTitle = "معاينة الخدمة",
                                        depositPercent = 25,
                                        cancellationPolicy = "استرداد كامل قبل 14 يوماً.",
                                        bookedDates = emptyList(),
                                        reviews = emptyList()
                                    )
                                )
                                serviceTitle = ""
                            }
                        },
                        enabled = serviceTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("حفظ ونشر الخدمة")
                    }
                }
            }
        }

        // الطلبات الواردة
        item {
            Text("الطلبات الواردة (${uiState.orders.size})", style = MaterialTheme.typography.titleLarge)
        }
        items(uiState.orders, key = { it.orderId }) { order ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${order.orderId} · ${order.eventTypeTitle} (${order.eventDate})", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OrderStatus.entries.forEach { st ->
                            FilterChip(
                                selected = order.statusName == st.name,
                                onClick = { onUpdateOrderStatus(order.orderId, st) },
                                label = { Text(st.titleAr) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * شاشة الإعدادات:
 * المكان الوحيد المخصص لتعديل العملة وأسعار الصرف وتبديل الحساب
 */
@Composable
fun SettingsScreen(
    uiState: HaflatiUiState,
    onSelectCurrency: (CurrencyType) -> Unit,
    onSaveExchangeRates: (yer: Double, usd: Double) -> Unit,
    onResetExchangeRates: () -> Unit,
    onUpdateSupportPhone: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    onSwitchAccount: () -> Unit
) {
    var yerRateText by remember(uiState.exchangeRates[CurrencyType.YER]) {
        mutableStateOf((uiState.exchangeRates[CurrencyType.YER] ?: 140.0).toString())
    }
    var usdRateText by remember(uiState.exchangeRates[CurrencyType.USD]) {
        mutableStateOf((uiState.exchangeRates[CurrencyType.USD] ?: 0.267).toString())
    }
    var phoneText by remember(uiState.supportPhone) { mutableStateOf(uiState.supportPhone) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("الإعدادات", style = MaterialTheme.typography.headlineMedium)
        }

        // 1. تعديل العملة وأسعار الصرف (في الإعدادات فقط)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.AttachMoney, contentDescription = null, tint = HaflatiSoftGold)
                        Text("تعديل العملة وأسعار الصرف", style = MaterialTheme.typography.titleLarge)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrencyType.entries.forEach { cur ->
                            val selected = uiState.currency == cur
                            Surface(
                                onClick = { onSelectCurrency(cur) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) HaflatiPurple else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        cur.symbolAr,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        cur.titleAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("سعر الصرف مقابل 1 ر.س", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onResetExchangeRates) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("افتراضي")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = yerRateText,
                            onValueChange = { yerRateText = it },
                            label = { Text("الريال اليمني (ر.ي)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = usdRateText,
                            onValueChange = { usdRateText = it },
                            label = { Text("الدولار ($)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            onSaveExchangeRates(
                                yerRateText.toDoubleOrNull() ?: 140.0,
                                usdRateText.toDoubleOrNull() ?: 0.267
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("حفظ تعديل العملة وأسعار الصرف")
                    }
                }
            }
        }

        // 2. الحساب الحالي وتبديل الحساب (في الإعدادات فقط)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "الحساب الحالي: ${uiState.userSession?.name ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "نوع الحساب: ${uiState.userSession?.role?.titleAr ?: "عميل"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onSwitchAccount,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("تبديل الحساب / تسجيل الخروج", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // 3. رقم خدمة العملاء والمظهر الداكن/الفاتح
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("التواصل والمظهر", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = phoneText,
                            onValueChange = { phoneText = it },
                            label = { Text("رقم خدمة العملاء") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { onUpdateSupportPhone(phoneText) },
                            colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple)
                        ) {
                            Text("حفظ")
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("المظهر (فاتح / داكن)", style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = onToggleDarkMode,
                            colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple)
                        ) {
                            Icon(
                                if (uiState.isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (uiState.isDarkMode) "الوضع الفاتح" else "الوضع الداكن")
                        }
                    }
                }
            }
        }

        // 4. شعار حفلتي الفاتح والداكن
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("هوية وشعار «حفلتي»", style = MaterialTheme.typography.titleLarge)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAF7F2), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        HaflatiLogo(isDarkSurface = false, showTagline = true)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF14101B), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        HaflatiLogo(isDarkSurface = true, showTagline = false)
                    }
                }
            }
        }
    }
}
