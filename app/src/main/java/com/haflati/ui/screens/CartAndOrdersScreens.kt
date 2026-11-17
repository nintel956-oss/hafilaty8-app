package com.haflati.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haflati.model.*
import com.haflati.ui.theme.HaflatiPurple
import com.haflati.ui.theme.HaflatiSoftGold
import com.haflati.viewmodel.HaflatiUiState

/**
 * 5. شاشة السلة مع متابعة الشراء (ملخص + اتصال + واتساب + مشاركة) وإلغاء
 */
@Composable
fun CartScreen(
    uiState: HaflatiUiState,
    onUpdateItem: (CartItemEntity) -> Unit,
    onRemoveItem: (String) -> Unit,
    onOpenCheckout: () -> Unit,
    onCancelAndContinueBrowsing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("سلة تجهيزات الحفلة", style = MaterialTheme.typography.headlineMedium)

        if (uiState.isOverBudget) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        "تنبيه: إجمالي التكلفة تجاوز الميزانية المحددة (${formatPriceInCurrency(uiState.wizardConfig.budget, uiState.currency, uiState.currentRate)})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "السلة فارغة حالياً، استعرض الأقسام واختر خدمات حفلتك.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.cartItems, key = { it.serviceId }) { item ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.serviceTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${item.categoryName} · التاريخ: ${item.selectedDate}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onRemoveItem(item.serviceId) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (item.quantity > 1) {
                                                onUpdateItem(item.copy(quantity = item.quantity - 1))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Remove, contentDescription = "إنقاص")
                                    }
                                    Text("${item.quantity}", style = MaterialTheme.typography.titleMedium)
                                    IconButton(
                                        onClick = { onUpdateItem(item.copy(quantity = item.quantity + 1)) }
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = "زيادة")
                                    }
                                }

                                Text(
                                    formatPriceInCurrency(item.unitPrice * item.quantity, uiState.currency, uiState.currentRate),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HaflatiPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الإجمالي الكلي")
                        Text(
                            formatPriceInCurrency(uiState.totalCartCost, uiState.currency, uiState.currentRate),
                            fontWeight = FontWeight.Bold,
                            color = HaflatiPurple
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("العربون المبدئي المطلوب")
                        Text(
                            formatPriceInCurrency(uiState.totalDepositRequired, uiState.currency, uiState.currentRate),
                            fontWeight = FontWeight.Bold,
                            color = HaflatiSoftGold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancelAndContinueBrowsing,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("إلغاء")
            }
            Button(
                onClick = onOpenCheckout,
                enabled = uiState.cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                modifier = Modifier
                    .weight(1.5f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("متابعة الشراء")
            }
        }
    }
}

/**
 * نافذة ملخص الحجز والربط المباشر مع خدمة العملاء (اتصال + واتساب + مشاركة + إلغاء)
 */
@Composable
fun CheckoutSupportDialog(
    uiState: HaflatiUiState,
    onCallSupport: () -> Unit,
    onWhatsAppSupport: () -> Unit,
    onShareSummary: () -> Unit,
    onConfirmOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ملخص الحجز وخدمة العملاء") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "لا يوجد دفع إلكتروني في هذه النسخة. يتم تأكيد الحجز والتعاقد الموحّد عبر موظف خدمة العملاء (${uiState.supportPhone}).",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
                Text(
                    "الخدمات المختارة: ${uiState.cartItems.size}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "الإجمالي: ${formatPriceInCurrency(uiState.totalCartCost, uiState.currency, uiState.currentRate)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = HaflatiPurple
                )
                Text(
                    "العربون: ${formatPriceInCurrency(uiState.totalDepositRequired, uiState.currency, uiState.currentRate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HaflatiSoftGold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCallSupport,
                        colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("اتصال")
                    }
                    Button(
                        onClick = onWhatsAppSupport,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("واتساب")
                    }
                }

                OutlinedButton(
                    onClick = onShareSummary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("مشاركة ملخص الحفلة")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmOrder,
                colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple)
            ) {
                Text("تأكيد وإرسال لطلباتي")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * شاشة المفضلة
 */
@Composable
fun FavoritesScreen(
    uiState: HaflatiUiState,
    onOpenService: (ServiceItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleCompare: (String) -> Unit
) {
    val favoriteServices = uiState.allServices.filter { it.id in uiState.favoriteIds }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("المفضلة (${favoriteServices.size})", style = MaterialTheme.typography.headlineMedium)
        }
        if (favoriteServices.isEmpty()) {
            item {
                Text(
                    "لم تحفظ أي خدمة في المفضلة بعد.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(favoriteServices, key = { it.id }) { service ->
                ServiceMobileCard(
                    service = service,
                    currency = uiState.currency,
                    rate = uiState.currentRate,
                    isFavorite = true,
                    isCompared = service.id in uiState.compareIds,
                    isMerchant = false,
                    onOpen = { onOpenService(service) },
                    onToggleFavorite = { onToggleFavorite(service.id) },
                    onToggleCompare = { onToggleCompare(service.id) }
                )
            }
        }
    }
}

/**
 * شاشة طلباتي — متابعة حالة الطلبات
 */
@Composable
fun OrdersScreen(uiState: HaflatiUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("طلباتي (${uiState.orders.size})", style = MaterialTheme.typography.headlineMedium)
        }
        if (uiState.orders.isEmpty()) {
            item {
                Text(
                    "لا توجد حجوزات مرسلة حتى الآن.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.orders, key = { it.orderId }) { order ->
                val status = OrderStatus.entries.firstOrNull { it.name == order.statusName } ?: OrderStatus.UNDER_REVIEW
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${order.orderId} · ${order.eventTypeTitle}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                status.titleAr,
                                style = MaterialTheme.typography.labelLarge,
                                color = when (status) {
                                    OrderStatus.CONFIRMED -> Color(0xFF15803D)
                                    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                    OrderStatus.UNDER_REVIEW -> HaflatiSoftGold
                                }
                            )
                        }
                        Text("التاريخ: ${order.eventDate} · ${order.createdAtLabel}", style = MaterialTheme.typography.bodyMedium)
                        Text(order.itemsSummaryJson, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "الإجمالي: ${formatPriceInCurrency(order.totalAmount, uiState.currency, uiState.currentRate)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = HaflatiPurple
                        )
                    }
                }
            }
        }
    }
}

/**
 * معالج البداية "ابدأ حفلتك" — 4 خطوات
 */
@Composable
fun PartyWizardDialog(
    uiState: HaflatiUiState,
    onSaveConfig: (PartyWizardConfig) -> Unit,
    onAddService: (ServiceItem, String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var eventType by remember { mutableStateOf(uiState.wizardConfig.eventType) }
    var eventDate by remember { mutableStateOf(uiState.wizardConfig.eventDate) }
    var guestCountText by remember { mutableStateOf(uiState.wizardConfig.guestCount.toString()) }
    var budgetText by remember { mutableStateOf(uiState.wizardConfig.budget.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("معالج «ابدأ حفلتك» — خطوة $step من 4") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (step) {
                    1 -> {
                        Text("ما نوع مناسبتك القادمة؟", style = MaterialTheme.typography.titleMedium)
                        EventType.entries.forEach { ev ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = eventType == ev, onClick = { eventType = ev })
                                Text(ev.titleAr)
                            }
                        }
                    }
                    2 -> {
                        Text("تاريخ المناسبة المتوقع", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> {
                        Text("عدد الضيوف والميزانية (بالريال السعودي)", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = guestCountText,
                            onValueChange = { guestCountText = it },
                            label = { Text("عدد الضيوف") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = budgetText,
                            onValueChange = { budgetText = it },
                            label = { Text("الميزانية التقديرية") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        Text("ترشيحات مناسبة لحفلتك", style = MaterialTheme.typography.titleMedium)
                        val recs = uiState.allServices
                            .filter { it.category in eventType.recommendedCategories && eventDate !in it.bookedDates }
                            .take(3)
                        recs.forEach { srv ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(srv.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text(formatPriceInCurrency(srv.startingPrice, uiState.currency, uiState.currentRate), color = HaflatiPurple)
                                }
                                TextButton(onClick = { onAddService(srv, eventDate) }) {
                                    Text("أضف")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 4) {
                        if (step == 3) {
                            onSaveConfig(
                                PartyWizardConfig(
                                    eventType = eventType,
                                    eventDate = eventDate,
                                    guestCount = guestCountText.toIntOrNull() ?: 150,
                                    budget = budgetText.toDoubleOrNull() ?: 35000.0,
                                    isCompleted = true
                                )
                            )
                        }
                        step++
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HaflatiPurple)
            ) {
                Text(if (step < 4) "التالي" else "إنهاء")
            }
        },
        dismissButton = {
            TextButton(onClick = { if (step > 1) step-- else onDismiss() }) {
                Text(if (step > 1) "السابق" else "إغلاق")
            }
        }
    )
}

/**
 * نافذة مقارنة خدمتين أو ثلاث جنباً إلى جنب
 */
@Composable
fun CompareDialog(
    uiState: HaflatiUiState,
    onOpenService: (ServiceItem) -> Unit,
    onDismiss: () -> Unit
) {
    val compared = uiState.allServices.filter { it.id in uiState.compareIds }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مقارنة الخدمات (${compared.size})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                compared.forEach { srv ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(srv.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "السعر: ${formatPriceInCurrency(srv.startingPrice, uiState.currency, uiState.currentRate)} · المدينة: ${srv.city}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "السعة: ${srv.capacity} · التقييم: ★ ${srv.rating} · العربون: ${srv.depositPercent}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = {
                                onDismiss()
                                onOpenService(srv)
                            }) {
                                Text("عرض التفاصيل")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
