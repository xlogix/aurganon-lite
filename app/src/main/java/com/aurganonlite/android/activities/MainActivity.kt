package com.aurganonlite.android.activities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.aurganonlite.android.R
import com.aurganonlite.android.helpers.MyAppWebViewClient
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : Activity(), EasyPermissions.PermissionCallbacks {

    private var mWebView: WebView? = null
    private val mCM: String? = null
    private var mUploadMessage: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)

    // Check if device is online
    private val isDeviceOnline: Boolean
        get() {
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout.setColorSchemeResources(R.color.red_primary, R.color.black, R.color.google_blue_500)

        swipeRefreshLayout.isRefreshing = true

        // Find the WebView
        mWebView = findViewById(R.id.webView)

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore the previous URL and history stack
            mWebView!!.restoreState(savedInstanceState)
        }

        if (Build.VERSION.SDK_INT >= 21) {
            mWebView!!.settings.mixedContentMode = 0
            mWebView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else if (Build.VERSION.SDK_INT > 19) {
            mWebView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 19) {
            mWebView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            mWebView!!.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        }
        // Webview Settings [START]
        // Stop local links and redirects from opening in browser instead of WebView
        mWebView!!.webViewClient = MyAppWebViewClient()

        mWebView!!.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        mWebView!!.isScrollbarFadingEnabled = true
        // Enable JavaScript (May have to disable due to Google Play policies)
        mWebView!!.settings.javaScriptEnabled = true
        // Enable FileUpload (Set these true to make file upload work)
        mWebView!!.settings.allowFileAccess = false
        mWebView!!.settings.allowFileAccessFromFileURLs = false
        // Other Settings
        mWebView!!.settings.loadsImagesAutomatically = true
        mWebView!!.settings.setAppCacheEnabled(true)
        mWebView!!.settings.setSupportZoom(true)
        mWebView!!.settings.builtInZoomControls = false
        mWebView!!.settings.setSupportMultipleWindows(true)

        // Accepts Cookies Now
        CookieManager.getInstance().setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView!!.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true)
        }
        // WebView Settings [STOP]

        mWebView!!.loadUrl(URL)
        delayedRefresh()

        // File Upload [START]
        mWebView!!.webChromeClient = object : WebChromeClient() {

            //For Android 4.1
            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                this@MainActivity.startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    MainActivity.FILE_CHOOSER_RESULT_CODE
                )
            }

            //For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                askPermission()
                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent?> = arrayOfNulls(0)

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)
                return true
            }
        }
        // File Upload [END]

        // Implementing onRefresh Listener. Trigger for manual refresh.
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            mWebView!!.reload()
            delayedRefresh()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    internal fun askPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.d(TAG, "Permission Granted!")
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.rationale_storage),
                RC_STORAGE_PERM, Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    // Make it look like website is loading!
    private fun delayedRefresh() {
        val timerThread = object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(6000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    runOnUiThread { swipeRefreshLayout.isRefreshing = false }
                }
            }
        }
        timerThread.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUMA!!.onReceiveValue(results)
            mUMA = null
        } else if (requestCode == RC_SETTINGS_SCREEN) {
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
                .show()
        } else {
            if (requestCode == FCR) {
                if (null == mUploadMessage) return
                val result = if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
                mUploadMessage!!.onReceiveValue(result)
                mUploadMessage = null
            }
        }
    }

    // Prevent the back-button from closing the app
    override fun onBackPressed() {
        if (mWebView!!.canGoBack()) {
            mWebView!!.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isDeviceOnline) {
            Log.d(TAG, "Network available")
            //Toast.makeText(MainActivity.this, "Device Online", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this@MainActivity, "Device Offline. Functionality may be limited", Toast.LENGTH_SHORT).show()
        }
    }

    // Easy Permissions Classes
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // Some permissions have been granted
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()

        private val URL = "http://www.aurganon.com"
        // private static final String URL = "https://www.fnplus.tech";
        private val RC_STORAGE_PERM = 100
        private val RC_SETTINGS_SCREEN = 125
        private val FILE_CHOOSER_RESULT_CODE = 1
        private val FCR = 1
    }
}
