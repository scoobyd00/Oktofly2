package com.oktofly.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.webkit.*
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OktoFlyApp() {
    val context = LocalContext.current
    var locationReady by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Fetch location once permission is granted
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            fetchLocation(context) { lat, lon, error ->
                latitude = lat
                longitude = lon
                locationError = error
                locationReady = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        Color(0xFF0D2137)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top App Bar
            OktoFlyTopBar(
                onRefresh = {
                    if (locationPermission.status.isGranted) {
                        isLoading = true
                        fetchLocation(context) { lat, lon, error ->
                            latitude = lat
                            longitude = lon
                            locationError = error
                            locationReady = true
                        }
                    }
                }
            )

            when {
                !locationPermission.status.isGranted -> {
                    PermissionScreen(
                        showRationale = locationPermission.status.shouldShowRationale,
                        onRequestPermission = { locationPermission.launchPermissionRequest() }
                    )
                }

                !locationReady -> {
                    LoadingScreen(message = "Getting your location…")
                }

                locationError != null -> {
                    ErrorScreen(
                        message = locationError!!,
                        onRetry = {
                            locationReady = false
                            fetchLocation(context) { lat, lon, error ->
                                latitude = lat
                                longitude = lon
                                locationError = error
                                locationReady = true
                            }
                        }
                    )
                }

                else -> {
                    // Build the oktofly URL with coordinates
                    val url = buildOktoflyUrl(latitude!!, longitude!!)
                    OktoFlyWebView(
                        url = url,
                        onPageStarted = { isLoading = true },
                        onPageFinished = { isLoading = false }
                    )
                }
            }
        }

        // Loading overlay on top of WebView
        if (isLoading && locationReady && locationError == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC0A1628)),
                contentAlignment = Alignment.Center
            ) {
                LoadingScreen(message = "Loading forecast…")
            }
        }
    }
}

@Composable
fun OktoFlyTopBar(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1628))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Drone icon area
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1565C0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = "Drone",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "OktoFly",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Drone Weather Forecast",
                    color = Color(0xFF90CAF9),
                    fontSize = 11.sp
                )
            }
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1565C0))
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PermissionScreen(showRationale: Boolean, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF1565C0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Location Required",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (showRationale)
                "OktoFly needs your location to show the drone weather forecast for your area. Please grant location access."
            else
                "To show you accurate drone flying conditions, OktoFly needs access to your location.",
            color = Color(0xFF90CAF9),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Grant Location Access", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LoadingScreen(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF42A5F5),
            modifier = Modifier.size(56.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = message,
            color = Color(0xFF90CAF9),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFEF5350),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Something went wrong",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            color = Color(0xFF90CAF9),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OktoFlyWebView(
    url: String,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Enable persistent cookies so login session survives app restarts
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            WebView(context).apply {
                cookieManager.setAcceptThirdPartyCookies(this, true)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    // Allow geolocation in WebView
                    setGeolocationEnabled(true)
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/121.0.0.0 Mobile Safari/537.36"
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onPageStarted()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Flush cookies to disk so session persists after app restart
                        CookieManager.getInstance().flush()
                        // Inject CSS to fix contrast — only target white-on-white text,
                        // leave all coloured elements (red/orange/blue time labels) untouched
                        val css = """
                            javascript:(function() {
                                var style = document.createElement('style');
                                style.type = 'text/css';
                                style.innerHTML = `
                                    /* Hide the nav bar since we have our own */
                                    .oktofly-nav, .okf-nav-bar, nav { display: none !important; }

                                    /* Only fix elements that have a white/near-white background
                                       AND white/light text — i.e. invisible text on white rows.
                                       We do this by targeting white backgrounds specifically
                                       and forcing dark text, but ONLY where no inline
                                       background-color is set (those are the coloured badges). */
                                    .okf-hour-row,
                                    .hour-row,
                                    .forecast-row,
                                    .time-slot,
                                    .slot-row {
                                        color: #111111 !important;
                                    }

                                    /* Fix white background rows that have no colour class */
                                    tr:not([class*="red"]):not([class*="orange"]):not([class*="blue"]):not([class*="green"]) td,
                                    div:not([class*="red"]):not([class*="orange"]):not([class*="blue"]):not([class*="green"]) > span {
                                        color: #111111 !important;
                                    }

                                    /* Specifically fix rows where background is white/light grey */
                                    [style*="background-color: rgb(255, 255, 255)"],
                                    [style*="background-color: white"],
                                    [style*="background-color: #fff"],
                                    [style*="background-color: #f"] {
                                        color: #111111 !important;
                                    }

                                    /* Fix the expanded detail cards which are light blue/white */
                                    [style*="background-color: rgb(227"],
                                    [style*="background-color: rgb(236"],
                                    [style*="background-color: rgb(240"],
                                    [style*="background-color: rgb(245"],
                                    [style*="background-color: rgb(248"],
                                    [style*="background-color: rgb(250"] {
                                        color: #111111 !important;
                                    }

                                    /* Preserve white text on coloured (red/orange/blue) badges */
                                    [style*="background-color: rgb(33, 150, 243)"] { color: #fff !important; }
                                    [style*="background-color: rgb(30, 136, 229)"] { color: #fff !important; }
                                    [style*="background-color: rgb(244, 67, 54)"]  { color: #fff !important; }
                                    [style*="background-color: rgb(229, 57, 53)"]  { color: #fff !important; }
                                    [style*="background-color: rgb(255, 152, 0)"]  { color: #fff !important; }
                                    [style*="background-color: rgb(251, 140, 0)"]  { color: #fff !important; }
                                `;
                                document.head.appendChild(style);
                            })()
                        """.trimIndent()
                        view?.loadUrl(css)
                        onPageFinished()
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val uri = request?.url?.toString() ?: return false
                        // Keep navigation within oktofly/airdata domains
                        return !(uri.contains("oktofly.com") || uri.contains("airdata.com"))
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?,
                        callback: GeolocationPermissions.Callback?
                    ) {
                        // Allow the site to use geolocation
                        callback?.invoke(origin, true, false)
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        return true // suppress console logs
                    }
                }

                loadUrl(url)
            }
        },
        update = { webView ->
            // Only reload if URL changes
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}

// ---- Helpers ----

fun buildOktoflyUrl(lat: Double, lon: Double): String {
    return "https://oktofly.com/?lat=$lat&lng=$lon"
}

@SuppressLint("MissingPermission")
fun fetchLocation(
    context: Context,
    onResult: (Double?, Double?, String?) -> Unit
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // Try last known first (fast)
    fusedClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                onResult(location.latitude, location.longitude, null)
            } else {
                // Fall back to a fresh request
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { freshLocation: Location? ->
                        if (freshLocation != null) {
                            onResult(freshLocation.latitude, freshLocation.longitude, null)
                        } else {
                            onResult(null, null, "Could not determine your location. Please ensure GPS is enabled.")
                        }
                    }
                    .addOnFailureListener { e ->
                        onResult(null, null, "Location error: ${e.message}")
                    }
            }
        }
        .addOnFailureListener { e ->
            onResult(null, null, "Location error: ${e.message}")
        }
}
