package com.haflati.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haflati.data.*
import com.haflati.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * حالة الواجهة الرئيسية الموحدة لتطبيق حفلتي
 */
data class HaflatiUiState(
    val userSession: UserSession? = null,
    val activeTab: HaflatiTab = HaflatiTab.HOME,
    val allServices: List<ServiceItem> = MockHaflatiRepository.getAllServices(),
    val merchantPosts: List<MerchantPost> = MockHaflatiRepository.getInitialMerchantPosts(),
    val readyPackages: List<ReadyPackage> = MockHaflatiRepository.getReadyPackages(),
    val selectedEventType: EventType? = null,
    val selectedCategory: ServiceCategory? = null,
    val selectedServiceForDetail: ServiceItem? = null,
    val searchQuery: String = "",
    val filterState: ServiceFilterState = ServiceFilterState(),
    val cartItems: List<CartItemEntity> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val compareIds: List<String> = emptyList(),
    val orders: List<BookingOrderEntity> = emptyList(),
    val wizardConfig: PartyWizardConfig = PartyWizardConfig(),
    val supportPhone: String = "+967770000123",
    val isDarkMode: Boolean = false,
    val currency: CurrencyType = CurrencyType.SAR,
    val exchangeRates: Map<CurrencyType, Double> = mapOf(
        CurrencyType.SAR to 1.0,
        CurrencyType.YER to 140.0,
        CurrencyType.USD to 0.267
    ),
    val showWizardDialog: Boolean = false,
    val showCompareDialog: Boolean = false,
    val showCheckoutSummary: Boolean = false,
    val toastMessage: String? = null
) {
    val totalCartCost: Double
        get() = cartItems.sumOf { it.unitPrice * it.quantity }

    val totalDepositRequired: Double
        get() = cartItems.sumOf { (it.unitPrice * it.quantity) * (it.depositPercent / 100.0) }

    val isOverBudget: Boolean
        get() = wizardConfig.budget > 0 && totalCartCost > wizardConfig.budget

    val completedCategoriesCount: Int
        get() = cartItems.map { it.categoryName }.distinct().size

    val currentRate: Double
        get() = exchangeRates[currency] ?: currency.defaultRateFromSar

    fun filteredServices(): List<ServiceItem> {
        val q = searchQuery.trim().lowercase()
        val filtered = allServices.filter { item ->
            val matchCat = selectedCategory == null || item.category == selectedCategory
            val matchQuery = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.city.lowercase().contains(q) ||
                item.description.lowercase().contains(q)
            val matchCity = filterState.selectedCity == "الكل" || item.city == filterState.selectedCity
            val matchPrice = item.startingPrice <= filterState.maxPrice
            val matchCapacity = item.capacity >= filterState.minCapacity
            val matchRating = item.rating >= filterState.minRating
            matchCat && matchQuery && matchCity && matchPrice && matchCapacity && matchRating
        }
        return when (filterState.sortOption) {
            SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.startingPrice }
            SortOption.RATING_HIGH_TO_LOW -> filtered.sortedByDescending { it.rating }
            SortOption.RECOMMENDED -> filtered.sortedWith(
                compareByDescending<ServiceItem> { it.isFeaturedOffer }.thenByDescending { it.rating }
            )
        }
    }
}

class HaflatiViewModel(
    private val cartDao: CartDao,
    private val favoriteDao: FavoriteDao,
    private val orderDao: OrderDao,
    private val merchantPostDao: MerchantPostDao,
    private val prefs: HaflatiPreferences
) : ViewModel() {

    private val initialSession = prefs.getUserSession()

    private val _localState = MutableStateFlow(
        HaflatiUiState(
            userSession = initialSession,
            activeTab = if (initialSession?.role == UserRole.MERCHANT) HaflatiTab.MERCHANT else HaflatiTab.HOME,
            supportPhone = prefs.getSupportPhone(),
            wizardConfig = PartyWizardConfig(budget = prefs.getBudget()),
            isDarkMode = prefs.isDarkMode(),
            currency = prefs.getCurrency(),
            exchangeRates = mapOf(
                CurrencyType.SAR to 1.0,
                CurrencyType.YER to prefs.getExchangeRate(CurrencyType.YER),
                CurrencyType.USD to prefs.getExchangeRate(CurrencyType.USD)
            )
        )
    )

    val uiState: StateFlow<HaflatiUiState> = combine(
        _localState,
        cartDao.observeCart(),
        favoriteDao.observeFavorites(),
        orderDao.observeOrders(),
        merchantPostDao.observePosts()
    ) { state, cart, favorites, orders, dbPosts ->
        val mergedPosts = (dbPosts.map {
            MerchantPost(it.id, it.authorName, it.title, it.caption, it.city, it.imageUri, it.createdAt)
        } + MockHaflatiRepository.getInitialMerchantPosts()).distinctBy { it.id }

        state.copy(
            cartItems = cart,
            favoriteIds = favorites.map { it.serviceId }.toSet(),
            orders = orders,
            merchantPosts = mergedPosts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _localState.value
    )

    fun login(name: String, role: UserRole) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val session = UserSession(cleanName, role)
        prefs.setUserSession(session)
        _localState.update {
            it.copy(
                userSession = session,
                activeTab = if (role == UserRole.MERCHANT) HaflatiTab.MERCHANT else HaflatiTab.HOME
            )
        }
    }

    /** تبديل الحساب متاح من شاشة الإعدادات فقط */
    fun logout() {
        prefs.setUserSession(null)
        _localState.update { it.copy(userSession = null, activeTab = HaflatiTab.HOME) }
    }

    fun selectTab(tab: HaflatiTab) {
        val role = _localState.value.userSession?.role
        if (role == UserRole.MERCHANT && tab in listOf(HaflatiTab.CART, HaflatiTab.FAVORITES, HaflatiTab.ORDERS)) {
            showToast("حساب التاجر مخصص للنشر والإدارة ولا يدعم التسوق")
            return
        }
        _localState.update { it.copy(activeTab = tab) }
    }

    fun onSearchQueryChange(query: String) {
        _localState.update { it.copy(searchQuery = query) }
    }

    fun updateFilterState(newFilter: ServiceFilterState) {
        _localState.update { it.copy(filterState = newFilter) }
    }

    fun selectEventType(eventType: EventType) {
        _localState.update {
            it.copy(selectedEventType = eventType, activeTab = HaflatiTab.CATEGORIES)
        }
    }

    fun selectCategory(category: ServiceCategory?) {
        _localState.update {
            it.copy(selectedCategory = category, activeTab = HaflatiTab.SERVICES)
        }
    }

    fun openServiceDetail(service: ServiceItem?) {
        _localState.update { it.copy(selectedServiceForDetail = service) }
    }

    fun setWizardVisible(visible: Boolean) {
        if (_localState.value.userSession?.role == UserRole.MERCHANT) return
        _localState.update { it.copy(showWizardDialog = visible) }
    }

    fun saveWizardConfig(config: PartyWizardConfig) {
        prefs.setBudget(config.budget)
        _localState.update {
            it.copy(
                wizardConfig = config.copy(isCompleted = true),
                selectedEventType = config.eventType
            )
        }
    }

    fun setCompareDialogVisible(visible: Boolean) {
        _localState.update { it.copy(showCompareDialog = visible) }
    }

    fun setCheckoutSummaryVisible(visible: Boolean) {
        _localState.update { it.copy(showCheckoutSummary = visible) }
    }

    fun clearToast() {
        _localState.update { it.copy(toastMessage = null) }
    }

    private fun showToast(message: String) {
        _localState.update { it.copy(toastMessage = message) }
    }

    fun addToCart(service: ServiceItem, selectedDate: String, quantity: Int = 1) {
        if (_localState.value.userSession?.role == UserRole.MERCHANT) {
            showToast("لا يمكن للتاجر التسوق أو الإضافة للسلة")
            return
        }
        if (selectedDate in service.bookedDates) {
            showToast("هذا التاريخ محجوز مسبقاً، اختر تاريخاً متاحاً")
            return
        }
        viewModelScope.launch {
            cartDao.upsertCartItem(
                CartItemEntity(
                    serviceId = service.id,
                    serviceTitle = service.title,
                    categoryName = service.category.titleAr,
                    unitPrice = service.startingPrice,
                    depositPercent = service.depositPercent,
                    quantity = quantity,
                    selectedDate = selectedDate
                )
            )
            showToast("تمت إضافة ${service.title} إلى السلة")
        }
    }

    fun addReadyPackageToCart(pkg: ReadyPackage, date: String) {
        if (_localState.value.userSession?.role == UserRole.MERCHANT) {
            showToast("حساب التاجر مخصص للنشر والإدارة فقط")
            return
        }
        val services = _localState.value.allServices.filter { it.id in pkg.serviceIds }
        viewModelScope.launch {
            services.forEach { service ->
                cartDao.upsertCartItem(
                    CartItemEntity(
                        serviceId = service.id,
                        serviceTitle = service.title,
                        categoryName = service.category.titleAr,
                        unitPrice = service.startingPrice,
                        depositPercent = service.depositPercent,
                        quantity = 1,
                        selectedDate = date
                    )
                )
            }
            showToast("تمت إضافة ${pkg.title} إلى السلة")
        }
    }

    fun updateCartItem(item: CartItemEntity) {
        viewModelScope.launch { cartDao.upsertCartItem(item) }
    }

    fun removeFromCart(serviceId: String) {
        viewModelScope.launch {
            cartDao.deleteCartItem(serviceId)
            showToast("تم حذف الخدمة من السلة")
        }
    }

    fun toggleFavorite(serviceId: String) {
        if (_localState.value.userSession?.role == UserRole.MERCHANT) {
            showToast("المفضلة متاحة لحساب العميل فقط")
            return
        }
        viewModelScope.launch {
            if (serviceId in uiState.value.favoriteIds) {
                favoriteDao.removeFavorite(serviceId)
            } else {
                favoriteDao.addFavorite(FavoriteEntity(serviceId = serviceId))
                showToast("تم الحفظ في المفضلة")
            }
        }
    }

    fun toggleCompare(serviceId: String) {
        _localState.update { state ->
            val current = state.compareIds.toMutableList()
            if (serviceId in current) current.remove(serviceId)
            else if (current.size < 3) current.add(serviceId)
            state.copy(compareIds = current)
        }
    }

    fun addReview(serviceId: String, rating: Float, comment: String) {
        val cleanComment = comment.trim()
        if (cleanComment.isEmpty()) return
        val author = _localState.value.userSession?.name ?: "عميل حفلتي"
        _localState.update { state ->
            val updated = state.allServices.map { srv ->
                if (srv.id != serviceId) srv
                else {
                    val newReview = ServiceReview(
                        id = "rev-${System.currentTimeMillis()}",
                        authorName = author,
                        rating = rating,
                        comment = cleanComment,
                        dateLabel = "الآن"
                    )
                    val allReviews = listOf(newReview) + srv.reviews
                    val newAvg = ((srv.rating * srv.reviewCount) + rating) / (srv.reviewCount + 1)
                    srv.copy(
                        reviews = allReviews,
                        reviewCount = srv.reviewCount + 1,
                        rating = (newAvg * 10f).toInt() / 10f
                    )
                }
            }
            val selectedUpdated = updated.firstOrNull { it.id == state.selectedServiceForDetail?.id }
            state.copy(allServices = updated, selectedServiceForDetail = selectedUpdated)
        }
        showToast("شكراً لإضافة تقييمك")
    }

    /** إضافة منشور بصورة للتاجر فقط */
    fun addMerchantPost(title: String, caption: String, city: String, imageUri: String) {
        val session = _localState.value.userSession ?: return
        if (session.role != UserRole.MERCHANT) {
            showToast("إضافة الصور والمنشورات متاحة لحساب التاجر فقط")
            return
        }
        viewModelScope.launch {
            merchantPostDao.insertPost(
                MerchantPostEntity(
                    id = "post-${System.currentTimeMillis()}",
                    authorName = session.name,
                    title = title.trim(),
                    caption = caption.trim(),
                    city = city,
                    imageUri = imageUri,
                    createdAt = "الآن"
                )
            )
            showToast("تم نشر الصورة والمنشور بنجاح")
        }
    }

    /** إضافة أو تعديل خدمة للتاجر فقط */
    fun merchantUpsertService(service: ServiceItem) {
        if (_localState.value.userSession?.role != UserRole.MERCHANT) return
        _localState.update { state ->
            val list = state.allServices.toMutableList()
            val idx = list.indexOfFirst { it.id == service.id }
            if (idx >= 0) list[idx] = service else list.add(0, service)
            state.copy(allServices = list)
        }
        showToast("تم حفظ بيانات الخدمة")
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderDao.updateOrderStatus(orderId, status.name)
            showToast("تم تحديث حالة الطلب $orderId")
        }
    }

    fun submitBookingOrder() {
        val current = uiState.value
        if (current.cartItems.isEmpty() || current.userSession?.role == UserRole.MERCHANT) return
        viewModelScope.launch {
            val newOrder = BookingOrderEntity(
                orderId = "HF-${UUID.randomUUID().toString().take(6).uppercase()}",
                createdAtLabel = "الآن",
                eventTypeTitle = (current.selectedEventType ?: current.wizardConfig.eventType).titleAr,
                eventDate = current.wizardConfig.eventDate,
                itemsSummaryJson = current.cartItems.joinToString(" + ") { it.serviceTitle },
                totalAmount = current.totalCartCost,
                depositAmount = current.totalDepositRequired,
                statusName = OrderStatus.UNDER_REVIEW.name
            )
            orderDao.insertOrder(newOrder)
            cartDao.clearCart()
            _localState.update {
                it.copy(showCheckoutSummary = false, activeTab = HaflatiTab.ORDERS)
            }
            showToast("تم إرسال طلبك بنجاح وتوثيقه في طلباتي")
        }
    }

    fun buildCartSummaryText(): String {
        val s = uiState.value
        val rate = s.currentRate
        val lines = s.cartItems.joinToString("\n") { item ->
            val converted = (item.unitPrice * item.quantity * rate).toInt()
            "• ${item.serviceTitle} (${item.selectedDate}) — $converted ${s.currency.symbolAr}"
        }
        val total = (s.totalCartCost * rate).toInt()
        val deposit = (s.totalDepositRequired * rate).toInt()
        return """
            ملخص تجهيز حفلة من تطبيق حفلتي
            العميل: ${s.userSession?.name ?: ""}
            المناسبة: ${(s.selectedEventType ?: s.wizardConfig.eventType).titleAr}
            التاريخ: ${s.wizardConfig.eventDate}
            الخدمات:
            $lines
            الإجمالي: $total ${s.currency.symbolAr}
            العربون المطلوب: $deposit ${s.currency.symbolAr}
        """.trimIndent()
    }

    fun openWhatsAppSupport(context: Context) {
        val cleanPhone = uiState.value.supportPhone.replace("+", "").replace(" ", "")
        val url = "https://wa.me/$cleanPhone?text=${Uri.encode(buildCartSummaryText())}"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun callCustomerSupport(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${uiState.value.supportPhone}"))
        context.startActivity(intent)
    }

    fun shareCartSummary(context: Context) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildCartSummaryText())
        }
        context.startActivity(Intent.createChooser(sendIntent, "مشاركة ملخص الحفلة"))
    }

    fun updateSupportPhone(newPhone: String) {
        val clean = newPhone.trim().ifEmpty { "+967770000123" }
        prefs.setSupportPhone(clean)
        _localState.update { it.copy(supportPhone = clean) }
        showToast("تم حفظ رقم خدمة العملاء")
    }

    fun toggleDarkMode() {
        val next = !_localState.value.isDarkMode
        prefs.setDarkMode(next)
        _localState.update { it.copy(isDarkMode = next) }
    }

    /** تغيير العملة متاح من شاشة الإعدادات فقط */
    fun setCurrency(currency: CurrencyType) {
        prefs.setCurrency(currency)
        _localState.update { it.copy(currency = currency) }
    }

    /** تعديل سعر الصرف متاح من شاشة الإعدادات فقط */
    fun updateExchangeRate(currency: CurrencyType, newRate: Double) {
        if (newRate <= 0) return
        prefs.setExchangeRate(currency, newRate)
        _localState.update { state ->
            state.copy(exchangeRates = state.exchangeRates + (currency to newRate))
        }
    }

    fun resetDefaultExchangeRates() {
        prefs.setExchangeRate(CurrencyType.YER, CurrencyType.YER.defaultRateFromSar)
        prefs.setExchangeRate(CurrencyType.USD, CurrencyType.USD.defaultRateFromSar)
        _localState.update {
            it.copy(
                exchangeRates = mapOf(
                    CurrencyType.SAR to 1.0,
                    CurrencyType.YER to CurrencyType.YER.defaultRateFromSar,
                    CurrencyType.USD to CurrencyType.USD.defaultRateFromSar
                )
            )
        }
        showToast("تمت استعادة أسعار الصرف الافتراضية")
    }
}
