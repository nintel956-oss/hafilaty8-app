package com.haflati.data

import android.content.Context
import androidx.room.*
import com.haflati.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun observeCart(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE serviceId = :serviceId")
    suspend fun deleteCartItem(serviceId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_services")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(item: FavoriteEntity)

    @Query("DELETE FROM favorite_services WHERE serviceId = :serviceId")
    suspend fun removeFavorite(serviceId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM booking_orders ORDER BY orderId DESC")
    fun observeOrders(): Flow<List<BookingOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: BookingOrderEntity)

    @Query("UPDATE booking_orders SET statusName = :statusName WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, statusName: String)
}

@Dao
interface MerchantPostDao {
    @Query("SELECT * FROM merchant_posts")
    fun observePosts(): Flow<List<MerchantPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: MerchantPostEntity)
}

@Database(
    entities = [
        CartItemEntity::class,
        FavoriteEntity::class,
        BookingOrderEntity::class,
        MerchantPostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HaflatiDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun orderDao(): OrderDao
    abstract fun merchantPostDao(): MerchantPostDao
}

class HaflatiPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("haflati_prefs", Context.MODE_PRIVATE)

    fun getUserSession(): UserSession? {
        val name = prefs.getString("session_name", null) ?: return null
        val roleName = prefs.getString("session_role", UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name
        val role = UserRole.entries.firstOrNull { it.name == roleName } ?: UserRole.CUSTOMER
        return UserSession(name = name, role = role)
    }

    fun setUserSession(session: UserSession?) {
        if (session == null) {
            prefs.edit().remove("session_name").remove("session_role").apply()
        } else {
            prefs.edit()
                .putString("session_name", session.name)
                .putString("session_role", session.role.name)
                .apply()
        }
    }

    fun getSupportPhone(): String =
        prefs.getString("support_phone", "+967770000123") ?: "+967770000123"

    fun setSupportPhone(phone: String) =
        prefs.edit().putString("support_phone", phone).apply()

    fun getBudget(): Double = prefs.getFloat("party_budget", 35000f).toDouble()
    fun setBudget(budget: Double) = prefs.edit().putFloat("party_budget", budget.toFloat()).apply()

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
    fun setDarkMode(dark: Boolean) = prefs.edit().putBoolean("dark_mode", dark).apply()

    fun getCurrency(): CurrencyType {
        val code = prefs.getString("currency_code", CurrencyType.SAR.name)
        return CurrencyType.entries.firstOrNull { it.name == code } ?: CurrencyType.SAR
    }

    fun setCurrency(currency: CurrencyType) {
        prefs.edit().putString("currency_code", currency.name).apply()
    }

    fun getExchangeRate(currency: CurrencyType): Double =
        prefs.getFloat("rate_${currency.name}", currency.defaultRateFromSar.toFloat()).toDouble()

    fun setExchangeRate(currency: CurrencyType, rate: Double) {
        prefs.edit().putFloat("rate_${currency.name}", rate.toFloat()).apply()
    }
}

object MockHaflatiRepository {

    val availableCities = listOf("الكل", "صنعاء", "عدن", "تعز", "إب", "المكلا", "الحديدة")

    fun getInitialMerchantPosts(): List<MerchantPost> = listOf(
        MerchantPost(
            id = "post-1",
            authorName = "قصر اللؤلؤة الملكي",
            title = "تجهيز كوشة موسم الشتاء الجديدة",
            caption = "تشكيلة ورود طبيعية وإضاءة دافئة متاحة الآن لحجوزات صنعاء وعدن.",
            city = "صنعاء",
            imageUri = "haflati_kosha_stage",
            createdAt = "منذ ساعتين"
        ),
        MerchantPost(
            id = "post-2",
            authorName = "موائد السلطان للضيافة",
            title = "بوفيه الضيافة اليمني والعربي الفاخر",
            caption = "قائمة حلويات وأطباق رئيسية محضّرة طازجاً للمناسبات الكبيرة.",
            city = "عدن",
            imageUri = "haflati_hospitality_buffet",
            createdAt = "أمس"
        )
    )

    fun getReadyPackages(): List<ReadyPackage> = listOf(
        ReadyPackage(
            id = "pkg-wedding-royal",
            title = "باقة الزفاف الملكية المتكاملة",
            eventType = EventType.WEDDING,
            description = "قصر اللؤلؤة الملكي + كوشة الأوركيد + تصوير سينمائي 4K + سيارة زفاف لؤلؤية.",
            serviceIds = listOf("hall-1", "kosha-1", "photo-1", "car-1"),
            originalPrice = 31700.0,
            discountedPrice = 27500.0
        ),
        ReadyPackage(
            id = "pkg-engagement-chic",
            title = "باقة الخطوبة الأنيقة",
            eventType = EventType.ENGAGEMENT,
            description = "منتجع الياسمين + كوشة الورد الأبيض + تصوير فوتوغرافي + ركن القهوة والضيافة.",
            serviceIds = listOf("hall-3", "kosha-2", "photo-2", "hosp-2"),
            originalPrice = 15300.0,
            discountedPrice = 13200.0
        ),
        ReadyPackage(
            id = "pkg-graduation-joy",
            title = "باقة التخرّج السعيد",
            eventType = EventType.GRADUATION,
            description = "ركن تنسيق التخرج + ضيافة عربية + دعوات رقمية وتوزيعات + نظام صوتيات.",
            serviceIds = listOf("kosha-3", "hosp-2", "inv-3", "light-2"),
            originalPrice = 7200.0,
            discountedPrice = 6150.0
        )
    )

    fun getAllServices(): List<ServiceItem> = listOf(
        // 1. قاعات واستراحات
        ServiceItem(
            id = "hall-1",
            category = ServiceCategory.HALLS,
            title = "قصر اللؤلؤة الملكي للاحتفالات",
            city = "صنعاء",
            startingPrice = 18500.0,
            capacity = 450,
            rating = 4.9f,
            reviewCount = 128,
            description = "قاعة أفراح فاخرة بثريات كريستال ومدخل خاص للعروسين مع جناح تجهيز متكامل وفريق إشراف طوال الحفل.",
            features = listOf("سعة حتى 450 ضيف", "جناح خاص للعروس", "مواقف سيارات مع خدمة صف", "مشرفات قاعة متواجدات"),
            imageResNames = listOf("haflati_hero_ballroom", "haflati_kosha_stage"),
            videoPreviewTitle = "جولة سينمائية داخل قصر اللؤلؤة",
            videoDuration = "01:45",
            depositPercent = 25,
            cancellationPolicy = "استرداد كامل للعربون قبل الموعد بـ 30 يوماً، و50% قبل 15 يوماً.",
            bookedDates = listOf("2026-12-10", "2026-12-17", "2026-12-24"),
            reviews = listOf(
                ServiceReview("r-h1", "سارة المنصور", 5.0f, "فخامة القاعة وترتيب الطاولات أبهرا جميع الضيوف.", "منذ أسبوعين"),
                ServiceReview("r-h2", "عبدالله الدوسري", 4.8f, "تعامل راقٍ واستلام وتنظيم ممتاز دون أي تأخير.", "منذ شهر")
            ),
            isFeaturedOffer = true,
            discountLabel = "تخفيض 15% لحجوزات الشهر"
        ),
        ServiceItem(
            id = "hall-2",
            category = ServiceCategory.HALLS,
            title = "قاعة الماسة الفندقية",
            city = "عدن",
            startingPrice = 14500.0,
            capacity = 300,
            rating = 4.8f,
            reviewCount = 94,
            description = "قاعة فندقية حديثة بتجهيزات صوتية وإضاءة مدمجة وإطلالة مميزة تناسب حفلات الزفاف والخطوبة.",
            features = listOf("سعة 300 ضيف", "غرفة كبار شخصيات", "إضاءة معمارية قابلة للتخصيص", "مدخل مستقل"),
            imageResNames = listOf("haflati_hero_ballroom", "haflati_hospitality_buffet"),
            videoPreviewTitle = "عرض قاعة الماسة الفندقية",
            videoDuration = "01:20",
            depositPercent = 20,
            cancellationPolicy = "تأجيل مجاني للموعد قبل الحفل بـ 21 يوماً.",
            bookedDates = listOf("2026-12-12", "2026-12-19"),
            reviews = listOf(
                ServiceReview("r-h3", "نورة الغامدي", 4.8f, "الخدمة الفندقية ممتازة والنظافة عالية جداً.", "منذ 3 أسابيع")
            )
        ),
        ServiceItem(
            id = "hall-3",
            category = ServiceCategory.HALLS,
            title = "منتجع واستراحة الياسمين",
            city = "تعز",
            startingPrice = 6800.0,
            capacity = 150,
            rating = 4.7f,
            reviewCount = 62,
            description = "استراحة راقية بحديقة خارجية ومجلس داخلي واسع مثالية لعقد القران وحفلات التخرج والخطوبة.",
            features = listOf("جلسات خارجية مضاءة", "مجلس داخلي مكيف", "مطبخ تحضيري مجهز", "مساحة ألعاب أطفال"),
            imageResNames = listOf("haflati_kosha_stage", "haflati_hero_ballroom"),
            videoPreviewTitle = "جولة في منتجع الياسمين",
            videoDuration = "01:10",
            depositPercent = 30,
            cancellationPolicy = "استرداد كامل قبل 14 يوماً من المناسبة.",
            bookedDates = listOf("2026-12-15", "2026-12-22"),
            reviews = emptyList()
        ),

        // 2. الكوشة والتنسيق والورد
        ServiceItem(
            id = "kosha-1",
            category = ServiceCategory.KOSHA_FLOWERS,
            title = "تصميم كوشة الأوركيد الملكية",
            city = "صنعاء",
            startingPrice = 5800.0,
            capacity = 450,
            rating = 4.9f,
            reviewCount = 105,
            description = "كوشة عصرية بأقواس ذهبية مصقولة وورد أوركيد وروز طبيعي مع تنسيق ممر العروس و20 طاولة.",
            features = listOf("ورد طبيعي 100%", "تنسيق ممر وطاولات", "إضاءة دافئة مدمجة", "بروفة تصميم ثلاثية الأبعاد"),
            imageResNames = listOf("haflati_kosha_stage", "haflati_hero_ballroom"),
            videoPreviewTitle = "تفاصيل تركيب كوشة الأوركيد",
            videoDuration = "01:35",
            depositPercent = 30,
            cancellationPolicy = "تعديل الألوان والتصميم متاح قبل الحفل بـ 10 أيام.",
            bookedDates = listOf("2026-12-11", "2026-12-18"),
            reviews = listOf(
                ServiceReview("r-k1", "مها القحطاني", 5.0f, "الورد كان طازجاً والتنفيذ مطابق للتصميم تماماً.", "منذ 10 أيام")
            ),
            isFeaturedOffer = true,
            discountLabel = "الأكثر حجزاً"
        ),
        ServiceItem(
            id = "kosha-2",
            category = ServiceCategory.KOSHA_FLOWERS,
            title = "باقة تنسيق كلاسيك بالورد الأبيض",
            city = "عدن",
            startingPrice = 3500.0,
            capacity = 250,
            rating = 4.8f,
            reviewCount = 57,
            description = "خلفية مخملية ناعمة مع شمعدانات ذهبية وتنسيق طاولات أنيق يلائم الخطوبة وعقد القران.",
            features = listOf("باقة يد للعروس هدية", "تنسيق 12 طاولة", "شمعدانات كريستال", "تركيب وفك شامل"),
            imageResNames = listOf("haflati_kosha_stage"),
            videoPreviewTitle = "نموذج التنسيق الكلاسيكي",
            videoDuration = "00:55",
            depositPercent = 25,
            cancellationPolicy = "استرداد 80% قبل 14 يوماً.",
            bookedDates = listOf("2026-12-20"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "kosha-3",
            category = ServiceCategory.KOSHA_FLOWERS,
            title = "استوديو لافندر لتنسيق التخرج",
            city = "المكلا",
            startingPrice = 2200.0,
            capacity = 150,
            rating = 4.7f,
            reviewCount = 43,
            description = "ركن تصوير مميز لحفلات التخرج مع كتابة الاسم بالإضاءة وقوس ورد وبالونات وطاولة ضيافة.",
            features = listOf("لوحة اسم مضيئة", "طاولة كيك منسقة", "قوس بالونات وورد", "تركيب خلال ساعتين"),
            imageResNames = listOf("haflati_kosha_stage", "haflati_gifts_invites"),
            videoPreviewTitle = "تجهيز ركن حفل التخرج",
            videoDuration = "00:50",
            depositPercent = 20,
            cancellationPolicy = "إلغاء مجاني قبل 7 أيام.",
            bookedDates = listOf("2026-12-14"),
            reviews = emptyList()
        ),

        // 3. التصوير والفيديو
        ServiceItem(
            id = "photo-1",
            category = ServiceCategory.PHOTOGRAPHY,
            title = "عدسة الخلود للتصوير السينمائي 4K",
            city = "صنعاء",
            startingPrice = 4200.0,
            capacity = 500,
            rating = 4.9f,
            reviewCount = 112,
            description = "تغطية سينمائية كاملة بجودة 4K مع مصورتين، ألبوم إيطالي فاخر، وفيديو تشويقي خلال 48 ساعة.",
            features = listOf("فريق نسائي أو رجالي متكامل", "ألبوم مطبوع 40 صفحة", "مقطع تشويقي سريع", "تسليم رقمي آمن"),
            imageResNames = listOf("haflati_cinema_photo", "haflati_hero_ballroom"),
            videoPreviewTitle = "نموذج مونتاج زفاف سينمائي",
            videoDuration = "02:15",
            depositPercent = 25,
            cancellationPolicy = "تأجيل مجاني قبل 15 يوماً.",
            bookedDates = listOf("2026-12-18", "2026-12-25"),
            reviews = listOf(
                ServiceReview("r-p1", "أمل السبيعي", 5.0f, "الصور والفيديو تحفة فنية، وتعامل المصورات مريح جداً.", "منذ أسبوع")
            ),
            isFeaturedOffer = true,
            discountLabel = "شامل الألبوم الإيطالي"
        ),
        ServiceItem(
            id = "photo-2",
            category = ServiceCategory.PHOTOGRAPHY,
            title = "استوديو لمحة للتصوير الفوتوغرافي",
            city = "عدن",
            startingPrice = 2600.0,
            capacity = 300,
            rating = 4.7f,
            reviewCount = 69,
            description = "جلسة تصوير احترافية للعروسين مع تغطية لحظات الحفل وتصحيح ألوان سينمائي.",
            features = listOf("250 صورة معدلة", "إضاءة استوديو متنقلة", "ألبوم ميني هدية", "مدة تغطية 6 ساعات"),
            imageResNames = listOf("haflati_cinema_photo"),
            videoPreviewTitle = "جولة في أعمال استوديو لمحة",
            videoDuration = "01:05",
            depositPercent = 20,
            cancellationPolicy = "استرداد كامل قبل 10 أيام.",
            bookedDates = listOf("2026-12-13"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "photo-3",
            category = ServiceCategory.PHOTOGRAPHY,
            title = "توثيق الدرون واللقطات الجوية",
            city = "الحديدة",
            startingPrice = 1900.0,
            capacity = 400,
            rating = 4.6f,
            reviewCount = 34,
            description = "تغطية جوية لموكب السيارات والمداخل والحفلات الخارجية بتقنية 4K مرخصة.",
            features = listOf("تصريح تصوير جوي", "مونتاج دقيقتين", "مناسب للاستراحات والمواكب"),
            imageResNames = listOf("haflati_cinema_photo", "haflati_wedding_car"),
            videoPreviewTitle = "لقطات جوية لموكب زفاف",
            videoDuration = "01:00",
            depositPercent = 25,
            cancellationPolicy = "إلغاء مجاني في حال سوء الأحوال الجوية.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),

        // 4. الضيافة والبوفيه
        ServiceItem(
            id = "hosp-1",
            category = ServiceCategory.HOSPITALITY,
            title = "موائد السلطان للبوفيه الملكي",
            city = "صنعاء",
            startingPrice = 9500.0,
            capacity = 300,
            rating = 4.9f,
            reviewCount = 140,
            description = "بوفيه مفتوح شامل الأطباق العربية والعالمية، ركن حلويات فاخرة ومباشرين بزي رسمي.",
            features = listOf("يكفي 150 شخص (قابل للزيادة)", "12 صنف رئيسي ومقبلات", "فريق تقديم متكامل", "أدوات ضيافة نحاسية"),
            imageResNames = listOf("haflati_hospitality_buffet"),
            videoPreviewTitle = "عرض تجهيز البوفيه الملكي",
            videoDuration = "01:30",
            depositPercent = 30,
            cancellationPolicy = "تعديل عدد الضيوف متاح قبل 5 أيام.",
            bookedDates = listOf("2026-12-17"),
            reviews = listOf(
                ServiceReview("r-ho1", "خالد الحربي", 4.9f, "الطعام ساخن والتقديم يبيض الوجه أمام الضيوف.", "منذ 5 أيام")
            ),
            isFeaturedOffer = true,
            discountLabel = "ركن قهوة مجاني"
        ),
        ServiceItem(
            id = "hosp-2",
            category = ServiceCategory.HOSPITALITY,
            title = "ركن القهوة المختصة والضيافة العربية",
            city = "عدن",
            startingPrice = 2400.0,
            capacity = 250,
            rating = 4.8f,
            reviewCount = 77,
            description = "قهوة عربية بالهيل والزعفران، شاي مختص، تمور محشوة وحلويات شرقية مع مضيفين.",
            features = listOf("باريستا ومضيفات", "أكواب مطبوعة باسم المناسبة", "تمور فاخرة وبقلاوة", "خدمة 5 ساعات"),
            imageResNames = listOf("haflati_hospitality_buffet", "haflati_gifts_invites"),
            videoPreviewTitle = "تجربة ركن الضيافة العربية",
            videoDuration = "00:55",
            depositPercent = 20,
            cancellationPolicy = "إلغاء مجاني قبل 7 أيام.",
            bookedDates = listOf("2026-12-19"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "hosp-3",
            category = ServiceCategory.HOSPITALITY,
            title = "كيكة الحفل متعددة الطوابق والحلويات",
            city = "إب",
            startingPrice = 1850.0,
            capacity = 200,
            rating = 4.7f,
            reviewCount = 49,
            description = "كيكة زفاف أو تخرج 4 طوابق بتصميم مخصص مع طاولة حلويات فرنسية وميني تارت.",
            features = listOf("نكهات فانيلا، ريد فيلفيت، شوكولاتة", "توصيل مبرد للقاعة", "كتابة الأسماء بالذهب"),
            imageResNames = listOf("haflati_hospitality_buffet"),
            videoPreviewTitle = "تفاصيل تجهيز الكيك والحلويات",
            videoDuration = "00:45",
            depositPercent = 35,
            cancellationPolicy = "تأكيد التصميم قبل 6 أيام.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),

        // 5. الزفّات والفرق والفقرات
        ServiceItem(
            id = "zaffa-1",
            category = ServiceCategory.ZAFFA_BANDS,
            title = "زفّة المراسم الخاصة بتسجيل استوديو",
            city = "صنعاء",
            startingPrice = 2900.0,
            capacity = 500,
            rating = 4.9f,
            reviewCount = 88,
            description = "زفة حصرية مسجلة بأسماء العروسين مع مؤثرات صوتية وتنسيق دخول متكامل على المسرح.",
            features = listOf("تسجيل حصري بالأسماء", "بدون موسيقى أو بموسيقى حسب الطلب", "منسقة دخول مع العروس"),
            imageResNames = listOf("haflati_hero_ballroom", "haflati_kosha_stage"),
            videoPreviewTitle = "استمع لنموذج زفة المراسم",
            videoDuration = "01:15",
            depositPercent = 25,
            cancellationPolicy = "تعديل الأسماء والمقطع قبل 10 أيام.",
            bookedDates = listOf("2026-12-18"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "zaffa-2",
            category = ServiceCategory.ZAFFA_BANDS,
            title = "فرقة ليالي اليمن والبرع التراثي",
            city = "تعز",
            startingPrice = 4500.0,
            capacity = 500,
            rating = 4.8f,
            reviewCount = 64,
            description = "فرقة تراثية متكاملة للبرع والزامل اليمني واستقبال الضيوف بزي تراثي موحد وأداء حي.",
            features = listOf("12 مؤدي مع إيقاعات", "استقبال الموكب عند المدخل", "فقرتان خلال الحفل"),
            imageResNames = listOf("haflati_hero_ballroom"),
            videoPreviewTitle = "مقتطفات من أداء الفرقة",
            videoDuration = "01:40",
            depositPercent = 30,
            cancellationPolicy = "استرداد 75% قبل 14 يوماً.",
            bookedDates = listOf("2026-12-11"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "zaffa-3",
            category = ServiceCategory.ZAFFA_BANDS,
            title = "منسقة موسيقى DJ نسائية محترفة",
            city = "عدن",
            startingPrice = 2200.0,
            capacity = 350,
            rating = 4.7f,
            reviewCount = 52,
            description = "قائمة أغاني مخصصة لذوق المناسبة مع إدارة فقرات التخرج والزفاف وتنظيم التوقيت.",
            features = listOf("تجهيز قائمة مسبقة", "أجهزة تحكم حديثة", "التزام كامل بجدول الحفل"),
            imageResNames = listOf("haflati_cinema_photo"),
            videoPreviewTitle = "تنظيم فقرات الـ DJ",
            videoDuration = "00:50",
            depositPercent = 20,
            cancellationPolicy = "تأجيل مجاني قبل 7 أيام.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),

        // 6. تأجير السيارات
        ServiceItem(
            id = "car-1",
            category = ServiceCategory.CARS,
            title = "بنتلي فلاينج سبير اللؤلؤية مع سائق",
            city = "صنعاء",
            startingPrice = 3200.0,
            capacity = 4,
            rating = 5.0f,
            reviewCount = 61,
            description = "سيارة زفاف فاخرة مزينة بشرائط الحرير والورد الطبيعي مع سائق بزي رسمي لمدة 6 ساعات.",
            features = listOf("سائق خاص محترف", "تزيين ورد طبيعي", "ضيافة مياه وعصائر داخل السيارة", "شامل الوقود"),
            imageResNames = listOf("haflati_wedding_car"),
            videoPreviewTitle = "استعراض سيارة الزفاف اللؤلؤية",
            videoDuration = "00:55",
            depositPercent = 25,
            cancellationPolicy = "استرداد كامل قبل 7 أيام.",
            bookedDates = listOf("2026-12-17"),
            reviews = listOf(
                ServiceReview("r-c1", "سلطان العلي", 5.0f, "السيارة جديدة والورد أنيق والسائق ملتزم بالوقت.", "منذ أسبوعين")
            )
        ),
        ServiceItem(
            id = "car-2",
            category = ServiceCategory.CARS,
            title = "مرسيدس S-Class الفاخرة للموكب",
            city = "عدن",
            startingPrice = 2100.0,
            capacity = 4,
            rating = 4.8f,
            reviewCount = 48,
            description = "فخامة وهدوء مثالي لموكب العروسين أو استقبال الضيوف المهمين مع تزيين اختياري.",
            features = listOf("موديل السنة", "تزيين أبيض ناعم", "تغطية 5 ساعات"),
            imageResNames = listOf("haflati_wedding_car"),
            videoPreviewTitle = "تفاصيل خدمة مرسيدس",
            videoDuration = "00:45",
            depositPercent = 20,
            cancellationPolicy = "إلغاء مجاني قبل 5 أيام.",
            bookedDates = listOf("2026-12-12"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "car-3",
            category = ServiceCategory.CARS,
            title = "موكب 3 سيارات عائلية VIP",
            city = "المكلا",
            startingPrice = 3900.0,
            capacity = 18,
            rating = 4.7f,
            reviewCount = 29,
            description = "نقل منظم ومريح لأهل العروسين من الفندق إلى القاعة والعودة في سيارات دفع رباعي فاخرة.",
            features = listOf("3 سيارات مع سائقين", "تنسيق مسار الموكب", "مقاعد جلد واسعة"),
            imageResNames = listOf("haflati_wedding_car"),
            videoPreviewTitle = "تنظيم موكب العائلة",
            videoDuration = "00:50",
            depositPercent = 25,
            cancellationPolicy = "استرداد كامل قبل 10 أيام.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),

        // 7. المشاغل والمكياج
        ServiceItem(
            id = "salon-1",
            category = ServiceCategory.SALONS_MAKEUP,
            title = "باقة العروس الملكية — خبيرة التجميل ريم",
            city = "صنعاء",
            startingPrice = 3800.0,
            capacity = 3,
            rating = 4.9f,
            reviewCount = 95,
            description = "مكياج عروس ثابت بأرقى الماركات العالمية مع تسريحة شعر، تركيب الطرحة، ومرافقة حتى القاعة.",
            features = listOf("بروفة مكياج مسبقة", "عناية بالبشرة قبل المكياج", "خدمة منزلية أو في الفندق"),
            imageResNames = listOf("haflati_kosha_stage", "haflati_gifts_invites"),
            videoPreviewTitle = "لمحات من تجهيز العروس",
            videoDuration = "01:10",
            depositPercent = 30,
            cancellationPolicy = "تأجيل مجاني قبل 14 يوماً.",
            bookedDates = listOf("2026-12-18"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "salon-2",
            category = ServiceCategory.SALONS_MAKEUP,
            title = "تجهيز أخوات العروس والخريجات",
            city = "عدن",
            startingPrice = 1600.0,
            capacity = 5,
            rating = 4.8f,
            reviewCount = 51,
            description = "باقة تشمل المكياج والتسريحة لـ 3 مرافقات أو خريجات في مكان واحد وبسرعة قياسية.",
            features = listOf("3 إطلالات كاملة", "رموش طبيعية", "تثبيت يدوم 12 ساعة"),
            imageResNames = listOf("haflati_gifts_invites"),
            videoPreviewTitle = "إطلالات السهرة والتخرج",
            videoDuration = "00:50",
            depositPercent = 20,
            cancellationPolicy = "إلغاء مجاني قبل 5 أيام.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "salon-3",
            category = ServiceCategory.SALONS_MAKEUP,
            title = "صالون الجوهرة للعناية المتكاملة",
            city = "تعز",
            startingPrice = 2400.0,
            capacity = 2,
            rating = 4.7f,
            reviewCount = 38,
            description = "برنامج عناية متكامل قبل الحفل بيوم يشمل العناية بالأظافر والشعر والبشرة مع تجهيز يوم الحفل.",
            features = listOf("جلسة نضارة للبشرة", "مكياج ناعم أو سهرة", "تسريحة عصرية"),
            imageResNames = listOf("haflati_gifts_invites", "haflati_kosha_stage"),
            videoPreviewTitle = "جولة في صالون الجوهرة",
            videoDuration = "01:00",
            depositPercent = 25,
            cancellationPolicy = "تعديل الموعد قبل 7 أيام.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),

        // 8. الدعوات والهدايا والتوزيعات
        ServiceItem(
            id = "inv-1",
            category = ServiceCategory.INVITES_GIFTS,
            title = "بطاقات دعوة مذهّبة + توزيعات عطرية",
            city = "صنعاء",
            startingPrice = 1750.0,
            capacity = 200,
            rating = 4.9f,
            reviewCount = 74,
            description = "100 بطاقة دعوة ورقية مطبوعة بختم ذهبي مع دعوة إلكترونية QR و100 علبة عطر تذكارية.",
            features = listOf("تصميم خط عربي مخصص", "باركود دخول القاعة", "100 توزيعة عطر فاخرة"),
            imageResNames = listOf("haflati_gifts_invites"),
            videoPreviewTitle = "تفاصيل الطباعة الذهبية والتوزيعات",
            videoDuration = "00:55",
            depositPercent = 30,
            cancellationPolicy = "تأكيد التصميم قبل الطباعة بيومين.",
            bookedDates = emptyList(),
            reviews = listOf(
                ServiceReview("r-i1", "هدى التميمي", 5.0f, "الخط العربي والختم الشمعي فائق الجمال.", "منذ 4 أيام")
            ),
            isFeaturedOffer = true,
            discountLabel = "دعوة إلكترونية مجانية"
        ),
        ServiceItem(
            id = "inv-2",
            category = ServiceCategory.INVITES_GIFTS,
            title = "توزيعات بخور وعود في علب مخملية",
            city = "إب",
            startingPrice = 1300.0,
            capacity = 150,
            rating = 4.8f,
            reviewCount = 44,
            description = "80 علبة توزيعات راقية تحتوي على كسرة عود فاخر مع بطاقة تهنئة مطبوعة بالأسماء.",
            features = listOf("عود طبيعي فاخر", "تغليف مخملي بألوان الحفل", "شحن سريع لجميع المدن"),
            imageResNames = listOf("haflati_gifts_invites", "haflati_hospitality_buffet"),
            videoPreviewTitle = "تغليف توزيعات العود",
            videoDuration = "00:40",
            depositPercent = 25,
            cancellationPolicy = "استرداد كامل قبل بدء التجهيز.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "inv-3",
            category = ServiceCategory.INVITES_GIFTS,
            title = "دعوة فيديو موشن جرافيك + نظام تأكيد حضور",
            city = "عدن",
            startingPrice = 650.0,
            capacity = 500,
            rating = 4.7f,
            reviewCount = 83,
            description = "دعوة رقمية احترافية قابلة للإرسال عبر واتساب مع رابط خريطة القاعة وتأكيد حضور الضيوف.",
            features = listOf("تسليم خلال 24 ساعة", "مقاس مخصص للجوال", "تتبع عدد المؤكدين"),
            imageResNames = listOf("haflati_gifts_invites"),
            videoPreviewTitle = "نموذج دعوة موشن جرافيك",
            videoDuration = "00:35",
            depositPercent = 50,
            cancellationPolicy = "تعديلين مجانيين على النص.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),

        // 9. الإضاءة والصوتيات
        ServiceItem(
            id = "light-1",
            category = ServiceCategory.LIGHTING_AUDIO,
            title = "حزمة الإضاءة المسرحية والليزر والضباب",
            city = "صنعاء",
            startingPrice = 3100.0,
            capacity = 500,
            rating = 4.9f,
            reviewCount = 66,
            description = "إضاءة احترافية لممر العروس والكوشة مع جهاز دخان أرضي ناعم للرقصة الأولى وشاشات عرض.",
            features = listOf("متابعة فني إضاءة طوال الحفل", "إضاءة دافئة للتصوير", "دخان أرضي آمن بدون رائحة"),
            imageResNames = listOf("haflati_hero_ballroom", "haflati_cinema_photo"),
            videoPreviewTitle = "تأثيرات الإضاءة والدخان الأرضي",
            videoDuration = "01:10",
            depositPercent = 25,
            cancellationPolicy = "إلغاء مجاني قبل 10 أيام.",
            bookedDates = listOf("2026-12-17"),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "light-2",
            category = ServiceCategory.LIGHTING_AUDIO,
            title = "نظام صوتيات متكامل للقاعات المفتوحة",
            city = "الحديدة",
            startingPrice = 1950.0,
            capacity = 350,
            rating = 4.7f,
            reviewCount = 40,
            description = "سماعات عالية النقاء مع مايكروفونات لاسلكية وخلاط صوت احترافي يضمن وضوح الصوت.",
            features = listOf("4 سماعات محيطية", "3 مايكروفونات لاسلكية", "فني صوت مرافق"),
            imageResNames = listOf("haflati_cinema_photo"),
            videoPreviewTitle = "اختبار نقاء الصوتيات",
            videoDuration = "00:45",
            depositPercent = 20,
            cancellationPolicy = "استرداد كامل قبل 7 أيام.",
            bookedDates = emptyList(),
            reviews = emptyList()
        ),
        ServiceItem(
            id = "light-3",
            category = ServiceCategory.LIGHTING_AUDIO,
            title = "شاشة LED عملاقة لعرض الذكريات والتهاني",
            city = "تعز",
            startingPrice = 2700.0,
            capacity = 400,
            rating = 4.8f,
            reviewCount = 31,
            description = "شاشة عالية الدقة توضع في المسرح أو الاستقبال لعرض صور العروسين أو الخريج طوال المناسبة.",
            features = listOf("دقة عرض 4K", "تركيب وفك شامل", "دعم عرض الصور والفيديو مباشرة"),
            imageResNames = listOf("haflati_hero_ballroom", "haflati_kosha_stage"),
            videoPreviewTitle = "نموذج عرض شاشة LED",
            videoDuration = "00:50",
            depositPercent = 25,
            cancellationPolicy = "تأجيل مجاني قبل 10 أيام.",
            bookedDates = emptyList(),
            reviews = emptyList()
        )
    )
}
