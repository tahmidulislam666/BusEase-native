package com.example.busease.data

object AppStrings {
    // Navigation Tabs
    fun routeTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রুট" else "Route"
    fun busesTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাসসমূহ" else "Buses"
    fun fareTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাড়া তালিকা" else "Fare"
    fun savedTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষিত" else "Saved"
    fun settingsTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সেটিংস" else "Settings"

    // App Bar
    fun appTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাস ইজ (BusEase)" else "BusEase"
    fun appSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ঢাকা লোকাল বাস ও ভাড়া নির্দেশিকা" else "Dhaka City Bus & Fare Guide"

    // Settings Screen
    fun settingsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ সেটিংস" else "Settings"
    fun appearanceSection(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "থিম ও ডিসপ্লে" else "Display & Theme"
    fun darkModeTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডার্ক মোড (Dark Mode)" else "Dark Mode"
    fun darkModeDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডার্ক মোড অন অথবা অফ রাখুন" else "Turn dark theme on or off"
    fun darkModeActive(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডার্ক মোড সক্রিয়" else "Dark mode enabled"
    fun lightModeActive(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লাইট মোড সক্রিয় (ডিফল্ট)" else "Light mode enabled (Default)"

    fun languageSection(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাষা পছন্দ" else "Language"
    fun languageTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপের ভাষা" else "App Language"
    fun languageDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাংলা অথবা ইংরেজি ভাষা নির্বাচন করুন" else "Switch between English and বাংলা"

    // Developer Details
    fun developerSection(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডেভেলপার পরিচিতি" else "Developer Details"
    fun developerName(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "তাহমিদুল ইসলাম" else "Tahmidul Islam"
    fun developerEmail(lang: AppLanguage) = "tahmidulislam666@gmail.com"
    fun developerPortfolio(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পোর্টফোলিও ও গিটহাব" else "GitHub & Portfolio"
    fun developerBio(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) 
        "ঢাকা সিটির প্রতিদিনের সাধারণ যাত্রীদের চলাচল সহজ এবং ঝামেলামুক্ত করার জন্য বাস ইজ (BusEase) তৈরি করা হয়েছে। যেকোনো পরামর্শ বা ফিডব্যাকের জন্য সরাসরি যোগাযোগ করতে পারেন।"
        else "BusEase was developed to provide Dhaka commuters with clean, instant route discoveries, offline-first bus transit itineraries, and accurate BRTA government fare estimations."

    // App Information Section
    fun aboutSection(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ সম্পর্কিত তথ্য" else "About BusEase"
    fun appVersionLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ সংস্করণ" else "App Version"
    fun appVersionValue(lang: AppLanguage) = "v1.0.0 (Release Build)"
    fun datasetLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডাটাবেজ কাভারেজ" else "Transit Dataset"
    fun datasetValue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "১৪০+ ঢাকার বাস এবং ৪,৬০০+ স্টপেজ ম্যাপিং" else "140+ Dhaka City buses & 4,600+ stop waypoints"
    fun brtaFareLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাড়ার নিয়মাবলী" else "Fare Calculation"
    fun brtaFareValue(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিআরটিএ সরকারি দূরত্ব-ভিত্তিক ভাড়া চার্ট ও সর্বনিম্ন ১০ টাকা নীতি" else "BRTA official distance-based fare chart & min ৳10 policy"
    
    // Search screens
    fun searchBusHint(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাসের নাম বা রুট খুঁজুন (বাংলা / English)..." else "Search bus name in English or বাংলা..."
    fun allBusesCount(count: Int, lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোট $count টি বাস উপলব্ধ" else "$count buses available"
    fun findRouteTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রুট অনুযায়ী বাস খুঁজুন" else "Find Buses by Route"
    fun fromLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুরুর স্থান (From)" else "From (Start Location)"
    fun toLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গন্তব্য স্থান (To)" else "To (Destination)"
    fun findBusesBtn(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাস খুঁজুন" else "Find Buses"
    fun clearBtn(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মুছুন" else "Clear"
    fun popularStops(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "জনপ্রিয় বাস স্টপসমূহ" else "Popular Stops"
    fun busesFound(count: Int, lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "$count টি বাস পাওয়া গেছে" else "$count bus(es) found"
    fun noBusFound(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো বাস পাওয়া যায়নি" else "No Direct Bus Found"
    fun tryOtherStops(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অন্য কোনো নিকটবর্তী স্টপ দিয়ে পুনরায় চেষ্টা করুন" else "Try searching with nearby pickup or drop-off stops"
    
    // Fare Calculator
    fun fareCalcTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিআরটিএ বাস ভাড়া হিসাব" else "BRTA Bus Fare Calculator"
    fun fareCalcDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ঢাকার সরকারি নির্ধারিত দূরত্ব ভিত্তিক বাস ভাড়া জানুন" else "Calculate official government distance-based bus fares"
    fun selectSourceDest(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উৎস ও গন্তব্য নির্বাচন" else "Select Source & Destination"
    fun sourceStop(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উৎস স্টপ (Source)" else "Source Stop"
    fun destStop(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গন্তব্য স্টপ (Destination)" else "Destination Stop"
    fun officialFare(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নির্ধারিত সরকারি ভাড়া" else "Official BRTA Fare"
    fun totalStops(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোট স্টপেজ" else "Total Stops"
    fun distance(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আনুমানিক দূরত্ব" else "Est. Distance"

    // Favorites
    fun savedTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষিত প্রিয় বাস" else "Saved Favorite Buses"
    fun savedEmptyTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো বাস সংরক্ষিত নেই" else "No Favorite Buses Yet"
    fun savedEmptyDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দ্রুত খুঁজে পেতে যেকোনো বাসের পাশে থাকা হার্ট আইকনে ট্যাপ করুন" else "Tap the heart icon on any bus card to save it here for quick access"

    // Nearby Buses Feature (1km radius)
    fun nearbyTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আশেপাশের বাস" else "Nearby"
    fun nearbyTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিকটস্থ ১ কি.মি.-এর বাস রুট" else "Buses within 1 km"
    fun nearbySubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার বর্তমান অবস্থানের ১ কিমি ব্যাসার্ধের ভেতরের সব স্টপেজ ও বাসের রুট" else "Routes with stops within a 1 km radius of your location"
    fun requestLocationBtn(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিকটবর্তী বাস খুঁজুন (GPS)" else "Find Nearby Routes (GPS)"
    fun locatingMsg(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার অবস্থান চিহ্নিত করা হচ্ছে..." else "Detecting your location..."
    fun locationPermDenied(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লোকেশন অনুমতি প্রয়োজন। নিকটবর্তী ১ কিমির বাস দেখতে লোকেশন চালু করুন।" else "Location permission is required to detect bus stops within 1 km."
    fun noNearbyBuses(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "১ কি.মি.-এর মধ্যে কোনো বাস স্টপ পাওয়া যায়নি" else "No bus stops found within 1 km"
    fun noNearbyBusesDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনি কি ঢাকার বাস রুটের ভৌগোলিক অঞ্চলের বাইরে আছেন? নিচের দ্রুত লোকেশন নির্বাচন করুন।" else "Are you outside Dhaka's transit coverage? Try picking a transit hub below."
    fun nearestStopLabel(stop: String, distM: Int, lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নিকটস্থ স্টপ: $stop (~$distM মি.)" else "Nearest stop: $stop (~${distM}m)"

    // Route Detail Fare Estimation Tool
    fun routeFareToolTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাড়া হিসাব টুল (এই রুটের জন্য)" else "Route Fare Estimator"
    fun routeFareToolSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুরু ও নামার স্টপ নির্বাচন করে আনুমানিক বাস ভাড়া ও দূরত্ব জানুন" else "Select pickup and drop-off stops along this route to estimate fare"
    fun routePickupLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উঠা / শুরুর স্টপ" else "Pickup Stop"
    fun routeDropoffLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নামা / গন্তব্য স্টপ" else "Drop-off Stop"
    fun estimatedFareLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আনুমানিক ভাড়া" else "Estimated Fare"
    fun brtaOfficialRateNote(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সরকারি বিআরটিএ চার্ট এবং সর্বনিম্ন ভাড়া ১০ টাকা নীতি অনুযায়ী" else "Based on BRTA distance rate (~৳2.45/km) & minimum fare ৳10"
    fun studentDiscountFare(fare: Int, lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শিক্ষার্থী হাফ পাস: ৳${fare / 2}" else "Student Half-Pass: ৳${fare / 2}"
}
