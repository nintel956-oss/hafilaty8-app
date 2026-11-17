package com.haflati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.haflati.data.HaflatiDatabase
import com.haflati.data.HaflatiPreferences
import com.haflati.model.CurrencyType
import com.haflati.model.HaflatiTab
import com.haflati.model.UserRole
import com.haflati.ui.screens.*
import com.haflati.ui.theme.HaflatiTheme
import com.haflati.viewmodel.HaflatiViewModel

/**
 * نقطة الدخول الرئيسية لتطبيق حفلتي للأندرويد (Native Kotlin + Jetpack Compose + RTL)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            HaflatiDatabase::class.java,
            "haflati_local_db"
        ).build()

        val prefs = HaflatiPreferences(applicationContext)
        val viewModel = HaflatiViewModel(
            cartDao = db.cartDao(),
            favoriteDao = db.favoriteDao(),
            orderDao = db.orderDao(),
            merchantPostDao = db.merchantPostDao(),
            prefs = prefs
        )

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.toastMessage) {
                val msg = uiState.toastMessage
                if (msg != null) {
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearToast()
                }
            }

            HaflatiTheme(darkTheme = uiState.isDarkMode) {
                val session = uiState.userSession
                if (session == null) {
                    LoginScreen(onLogin = viewModel::login)
                } else {
                    val isMerchant = session.role == UserRole.MERCHANT

                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            HaflatiTopAppBar(
                                userSession = session,
                                showBack = uiState.activeTab == HaflatiTab.SERVICES ||
                                    uiState.activeTab == HaflatiTab.CART ||
                                    (isMerchant && uiState.activeTab == HaflatiTab.SETTINGS),
                                onBack = {
                                    when (uiState.activeTab) {
                                        HaflatiTab.SERVICES -> viewModel.selectTab(HaflatiTab.CATEGORIES)
                                        HaflatiTab.SETTINGS -> viewModel.selectTab(
                                            if (isMerchant) HaflatiTab.MERCHANT else HaflatiTab.HOME
                                        )
                                        else -> viewModel.selectTab(HaflatiTab.HOME)
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            Column {
                                if (!isMerchant) {
                                    PersistentTotalCostBar(
                                        totalCost = uiState.totalCartCost,
                                        itemCount = uiState.cartItems.size,
                                        isOverBudget = uiState.isOverBudget,
                                        currency = uiState.currency,
                                        exchangeRate = uiState.currentRate,
                                        onOpenCart = { viewModel.selectTab(HaflatiTab.CART) }
                                    )
                                }

                                NavigationBar {
                                    if (isMerchant) {
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.HOME,
                                            onClick = { viewModel.selectTab(HaflatiTab.HOME) },
                                            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                                            label = { Text("الرئيسية") }
                                        )
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.CATEGORIES || uiState.activeTab == HaflatiTab.SERVICES,
                                            onClick = { viewModel.selectTab(HaflatiTab.CATEGORIES) },
                                            icon = { Icon(Icons.Rounded.GridView, contentDescription = null) },
                                            label = { Text("الأقسام") }
                                        )
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.MERCHANT || uiState.activeTab == HaflatiTab.SETTINGS,
                                            onClick = { viewModel.selectTab(HaflatiTab.MERCHANT) },
                                            icon = { Icon(Icons.Rounded.Store, contentDescription = null) },
                                            label = { Text("التاجر") }
                                        )
                                    } else {
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.HOME,
                                            onClick = { viewModel.selectTab(HaflatiTab.HOME) },
                                            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                                            label = { Text("الرئيسية") }
                                        )
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.CATEGORIES || uiState.activeTab == HaflatiTab.SERVICES,
                                            onClick = { viewModel.selectTab(HaflatiTab.CATEGORIES) },
                                            icon = { Icon(Icons.Rounded.GridView, contentDescription = null) },
                                            label = { Text("الأقسام") }
                                        )
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.FAVORITES,
                                            onClick = { viewModel.selectTab(HaflatiTab.FAVORITES) },
                                            icon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
                                            label = { Text("المفضلة") }
                                        )
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.ORDERS,
                                            onClick = { viewModel.selectTab(HaflatiTab.ORDERS) },
                                            icon = { Icon(Icons.Rounded.ReceiptLong, contentDescription = null) },
                                            label = { Text("طلباتي") }
                                        )
                                        NavigationBarItem(
                                            selected = uiState.activeTab == HaflatiTab.SETTINGS,
                                            onClick = { viewModel.selectTab(HaflatiTab.SETTINGS) },
                                            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                            label = { Text("الإعدادات") }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (uiState.activeTab) {
                                HaflatiTab.HOME -> HomeScreen(
                                    uiState = uiState,
                                    onSearchChange = viewModel::onSearchQueryChange,
                                    onSearchSubmit = { viewModel.selectCategory(null) },
                                    onStartWizard = { viewModel.setWizardVisible(true) },
                                    onOpenMerchantPublish = { viewModel.selectTab(HaflatiTab.MERCHANT) },
                                    onEventClick = viewModel::selectEventType,
                                    onAddPackage = { pkg ->
                                        viewModel.addReadyPackageToCart(pkg, uiState.wizardConfig.eventDate)
                                    },
                                    onServiceClick = { viewModel.openServiceDetail(it) }
                                )

                                HaflatiTab.CATEGORIES -> CategoriesScreen(
                                    selectedEventType = uiState.selectedEventType,
                                    onSelectCategory = { viewModel.selectCategory(it) },
                                    onShowAllServices = { viewModel.selectCategory(null) }
                                )

                                HaflatiTab.SERVICES -> ServiceListScreen(
                                    uiState = uiState,
                                    onSearchChange = viewModel::onSearchQueryChange,
                                    onFilterChange = viewModel::updateFilterState,
                                    onSelectCategory = viewModel::selectCategory,
                                    onOpenService = { viewModel.openServiceDetail(it) },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onToggleCompare = viewModel::toggleCompare,
                                    onOpenCompareDialog = { viewModel.setCompareDialogVisible(true) }
                                )

                                HaflatiTab.FAVORITES -> FavoritesScreen(
                                    uiState = uiState,
                                    onOpenService = { viewModel.openServiceDetail(it) },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onToggleCompare = viewModel::toggleCompare
                                )

                                HaflatiTab.CART -> CartScreen(
                                    uiState = uiState,
                                    onUpdateItem = viewModel::updateCartItem,
                                    onRemoveItem = viewModel::removeFromCart,
                                    onOpenCheckout = { viewModel.setCheckoutSummaryVisible(true) },
                                    onCancelAndContinueBrowsing = { viewModel.selectTab(HaflatiTab.CATEGORIES) }
                                )

                                HaflatiTab.ORDERS -> OrdersScreen(uiState = uiState)

                                HaflatiTab.MERCHANT -> MerchantDashboardScreen(
                                    uiState = uiState,
                                    onAddPost = viewModel::addMerchantPost,
                                    onUpsertService = viewModel::merchantUpsertService,
                                    onUpdateOrderStatus = viewModel::updateOrderStatus,
                                    onOpenSettings = { viewModel.selectTab(HaflatiTab.SETTINGS) }
                                )

                                HaflatiTab.SETTINGS -> SettingsScreen(
                                    uiState = uiState,
                                    onSelectCurrency = viewModel::setCurrency,
                                    onSaveExchangeRates = { yer, usd ->
                                        viewModel.updateExchangeRate(CurrencyType.YER, yer)
                                        viewModel.updateExchangeRate(CurrencyType.USD, usd)
                                    },
                                    onResetExchangeRates = viewModel::resetDefaultExchangeRates,
                                    onUpdateSupportPhone = viewModel::updateSupportPhone,
                                    onToggleDarkMode = viewModel::toggleDarkMode,
                                    onSwitchAccount = viewModel::logout
                                )
                            }
                        }
                    }

                    val detailService = uiState.selectedServiceForDetail
                    if (detailService != null) {
                        ServiceDetailSheet(
                            service = detailService,
                            defaultDate = uiState.wizardConfig.eventDate,
                            currency = uiState.currency,
                            rate = uiState.currentRate,
                            userRole = session.role,
                            isFavorite = detailService.id in uiState.favoriteIds,
                            onDismiss = { viewModel.openServiceDetail(null) },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onAddToCart = { srv, date -> viewModel.addToCart(srv, date) },
                            onAddReview = viewModel::addReview
                        )
                    }

                    if (uiState.showWizardDialog && !isMerchant) {
                        PartyWizardDialog(
                            uiState = uiState,
                            onSaveConfig = viewModel::saveWizardConfig,
                            onAddService = { srv, date -> viewModel.addToCart(srv, date) },
                            onDismiss = { viewModel.setWizardVisible(false) }
                        )
                    }

                    if (uiState.showCompareDialog) {
                        CompareDialog(
                            uiState = uiState,
                            onOpenService = { viewModel.openServiceDetail(it) },
                            onDismiss = { viewModel.setCompareDialogVisible(false) }
                        )
                    }

                    if (uiState.showCheckoutSummary && !isMerchant) {
                        CheckoutSupportDialog(
                            uiState = uiState,
                            onCallSupport = { viewModel.callCustomerSupport(context) },
                            onWhatsAppSupport = { viewModel.openWhatsAppSupport(context) },
                            onShareSummary = { viewModel.shareCartSummary(context) },
                            onConfirmOrder = viewModel::submitBookingOrder,
                            onDismiss = { viewModel.setCheckoutSummaryVisible(false) }
                        )
                    }
                }
            }
        }
    }
}
