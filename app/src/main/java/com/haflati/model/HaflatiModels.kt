package com.haflati.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * نوع الحساب في واجهة تسجيل الدخول:
 * عميل (يتصفح ويتسوق دون إضافة صور/منشورات) أو تاجر (ينشر الصور والمنشورات دون تسوق)
 */
enum class UserRole(val titleAr: String) {
    CUSTOMER("عميل"),
    MERCHANT("تاجر")
}

data class UserSession(
    val name: String,
    val role: UserRole
)

/**
 * العملات المدعومة في تطبيق حفلتي: الريال السعودي، الريال اليمني، والدولار الأمريكي
 */
enum class CurrencyType(val titleAr: String, val symbolAr: String, val defaultRateFromSar: Double) {
    SAR("الريال السعودي", "ر.س", 1.0),
    YER("الريال اليمني", "ر.ي", 140.0),
    USD("الدولار الأمريكي", "$", 0.267)
}

/**
 * تبويبات التنقل الرئيسية في التطبيق المخصص للجوال
 */
enum class HaflatiTab(val titleAr: String) {
    HOME("الرئيسية"),
    CATEGORIES("الأقسام"),
    SERVICES("الخدمات"),
    FAVORITES("المفضلة"),
    CART("السلة"),
    ORDERS("طلباتي"),
    MERCHANT("لوحة التاجر"),
    SETTINGS("الإعدادات")
}

/**
 * أنواع المناسبات المدعومة في تطبيق حفلتي
 */
enum class EventType(
    val titleAr: String,
    val subtitleAr: String,
    val recommendedCategories: List<ServiceCategory>
) {
    WEDDING(
        "زفاف",
        "قاعات ملكية، كوشة، تصوير وضيافة متكاملة",
        listOf(
            ServiceCategory.HALLS,
            ServiceCategory.KOSHA_FLOWERS,
            ServiceCategory.PHOTOGRAPHY,
            ServiceCategory.HOSPITALITY,
            ServiceCategory.ZAFFA_BANDS,
            ServiceCategory.CARS,
            ServiceCategory.SALONS_MAKEUP,
            ServiceCategory.INVITES_GIFTS,
            ServiceCategory.LIGHTING_AUDIO
        )
    ),
    ENGAGEMENT(
        "خطوبة",
        "تنسيق أنيق وجلسات تصوير وضيافة راقية",
        listOf(
            ServiceCategory.HALLS,
            ServiceCategory.KOSHA_FLOWERS,
            ServiceCategory.PHOTOGRAPHY,
            ServiceCategory.HOSPITALITY,
            ServiceCategory.SALONS_MAKEUP,
            ServiceCategory.INVITES_GIFTS
        )
    ),
    GRADUATION(
        "تخرّج",
        "استراحات، ركن تصوير، صوتيات وتوزيعات نجاح",
        listOf(
            ServiceCategory.HALLS,
            ServiceCategory.KOSHA_FLOWERS,
            ServiceCategory.PHOTOGRAPHY,
            ServiceCategory.HOSPITALITY,
            ServiceCategory.INVITES_GIFTS,
            ServiceCategory.LIGHTING_AUDIO
        )
    ),
    AQD_QIRAN(
        "عقد قران",
        "أجواء هادئة، ضيافة عربية وتصوير تذكاري",
        listOf(
            ServiceCategory.HALLS,
            ServiceCategory.KOSHA_FLOWERS,
            ServiceCategory.PHOTOGRAPHY,
            ServiceCategory.HOSPITALITY,
            ServiceCategory.INVITES_GIFTS,
            ServiceCategory.SALONS_MAKEUP
        )
    ),
    OTHER(
        "مناسبة أخرى",
        "استقبال مولود، ترقية، أو تجمّع عائلي",
        listOf(
            ServiceCategory.HALLS,
            ServiceCategory.HOSPITALITY,
            ServiceCategory.PHOTOGRAPHY,
            ServiceCategory.INVITES_GIFTS,
            ServiceCategory.LIGHTING_AUDIO
        )
    )
}

/**
 * أقسام الخدمات التسعة الأساسية في التطبيق
 */
enum class ServiceCategory(val titleAr: String, val shortDescAr: String) {
    HALLS("قاعات واستراحات", "قصور أفراح وقاعات فندقية واستراحات خاصة"),
    KOSHA_FLOWERS("الكوشة والتنسيق والورد", "تصميم الكوشة وممرات العروس والطاولات"),
    PHOTOGRAPHY("التصوير والفيديو", "توثيق سينمائي 4K وألبومات فاخرة"),
    HOSPITALITY("الضيافة والبوفيه", "بوفيهات مفتوحة، قهوة عربية وحلويات"),
    ZAFFA_BANDS("الزفّات والفرق والفقرات", "زفات خاصة، فرق شعبية ومنسق موسيقى"),
    CARS("تأجير السيارات", "سيارات فارهة مزينة مع سائق خاص"),
    SALONS_MAKEUP("المشاغل والمكياج", "تجهيز عرائس وتسريحات شعر احترافية"),
    INVITES_GIFTS("الدعوات والهدايا والتوزيعات", "دعوات إلكترونية وورقية وتوزيعات فاخرة"),
    LIGHTING_AUDIO("الإضاءة والصوتيات", "أنظمة صوت احترافية وإضاءة مسرحية")
}

/**
 * حالات الطلب في صفحة طلباتي
 */
enum class OrderStatus(val titleAr: String) {
    UNDER_REVIEW("قيد المراجعة"),
    CONFIRMED("مؤكد"),
    CANCELLED("ملغي")
}

/**
 * نموذج تقييم وتعليق الزبون على الخدمة
 */
data class ServiceReview(
    val id: String,
    val authorName: String,
    val rating: Float,
    val comment: String,
    val dateLabel: String
)

/**
 * نموذج الخدمة الرئيسية (الأسعار الأساسية بالريال السعودي وتحوّل تلقائياً للعملة المختارة)
 */
data class ServiceItem(
    val id: String,
    val category: ServiceCategory,
    val title: String,
    val city: String,
    val startingPrice: Double,
    val capacity: Int,
    val rating: Float,
    val reviewCount: Int,
    val description: String,
    val features: List<String>,
    val imageResNames: List<String>,
    val videoPreviewTitle: String,
    val videoDuration: String = "01:15",
    val depositPercent: Int,
    val cancellationPolicy: String,
    val bookedDates: List<String>,
    val reviews: List<ServiceReview>,
    val isFeaturedOffer: Boolean = false,
    val discountLabel: String? = null
)

/**
 * نموذج الباقة الجاهزة المخفضة التي تجمع عدة خدمات بضغطة واحدة
 */
data class ReadyPackage(
    val id: String,
    val title: String,
    val eventType: EventType,
    val description: String,
    val serviceIds: List<String>,
    val originalPrice: Double,
    val discountedPrice: Double
)

/**
 * منشور وصورة مضافة من قبل التاجر
 */
data class MerchantPost(
    val id: String,
    val authorName: String,
    val title: String,
    val caption: String,
    val city: String,
    val imageUri: String,
    val createdAt: String
)

@Entity(tableName = "merchant_posts")
data class MerchantPostEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val title: String,
    val caption: String,
    val city: String,
    val imageUri: String,
    val createdAt: String
)

/**
 * كيان عنصر السلة المحفوظ محلياً عبر Room Database
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val serviceId: String,
    val serviceTitle: String,
    val categoryName: String,
    val unitPrice: Double,
    val depositPercent: Int,
    val quantity: Int,
    val selectedDate: String
)

/**
 * كيان المفضلة المحفوظ محلياً عبر Room Database
 */
@Entity(tableName = "favorite_services")
data class FavoriteEntity(
    @PrimaryKey val serviceId: String,
    val addedAtTimestamp: Long = System.currentTimeMillis()
)

/**
 * كيان الطلب والحجز المحفوظ محلياً عبر Room Database
 */
@Entity(tableName = "booking_orders")
data class BookingOrderEntity(
    @PrimaryKey val orderId: String,
    val createdAtLabel: String,
    val eventTypeTitle: String,
    val eventDate: String,
    val itemsSummaryJson: String,
    val totalAmount: Double,
    val depositAmount: Double,
    val statusName: String
)

/**
 * تفضيلات معالج البداية "ابدأ حفلتك"
 */
data class PartyWizardConfig(
    val eventType: EventType = EventType.WEDDING,
    val eventDate: String = "2026-12-18",
    val guestCount: Int = 200,
    val budget: Double = 35000.0,
    val isCompleted: Boolean = false
)

/**
 * خيارات الفلترة والترتيب في قائمة الخدمات
 */
enum class SortOption(val titleAr: String) {
    RECOMMENDED("الأكثر ملائمة"),
    PRICE_LOW_TO_HIGH("الأقل سعراً"),
    RATING_HIGH_TO_LOW("الأعلى تقييماً")
}

data class ServiceFilterState(
    val selectedCity: String = "الكل",
    val maxPrice: Double = 25000.0,
    val minCapacity: Int = 0,
    val minRating: Float = 0f,
    val sortOption: SortOption = SortOption.RECOMMENDED
)
