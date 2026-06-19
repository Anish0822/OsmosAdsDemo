package com.example.osmosadsdemo.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.ai.osmos.core.OsmosSDK
import com.bumptech.glide.Glide
import com.example.osmosadsdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.collections.get

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val osmosSDK by lazy {
        OsmosSDK.globalInstance()
    }

    private val adFetcher by lazy {
        osmosSDK.adFetcherSDK()
    }

    private val registerEvent by lazy {
        osmosSDK.registerEvent()
    }

    private var isLoading = false
    private var impressionSent = false
    private var imageUrl: String? = null
    private var clickUrl: String? = null
    private var uclid: String? = null
    private var bannerWidth = 0
    private var bannerHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedInstanceState?.let {
            imageUrl = it.getString("imageUrl")
            clickUrl = it.getString("clickUrl")
            uclid = it.getString("uclid")
            bannerWidth = it.getInt("bannerWidth")
            bannerHeight = it.getInt("bannerHeight")
            impressionSent = it.getBoolean("impressionSent")
        }

        setupViews()
        setupListeners()

        if (!imageUrl.isNullOrEmpty()) {

            binding.ivBanner.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrl)
                .into(binding.ivBanner)
            binding.tvStatus.text = "Status: Ad Loaded"
            setupBanner()
        }

    }

    private fun setupViews() {
        binding.progressBar.visibility = View.GONE
        binding.ivBanner.visibility = View.GONE
        binding.tvStatus.text = "Status: Waiting"
    }

    private fun setupListeners() {
        binding.btnLoadAd.setOnClickListener {
            if (isLoading) return@setOnClickListener
            loadAd()
        }
    }

    private fun loadAd() {
        isLoading = true
        impressionSent = false
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLoadAd.isEnabled = false
        binding.tvStatus.text = "Status: Loading..."
        fetchBannerAd()
    }

    private fun fetchBannerAd() {
        lifecycleScope.launch {
            try {
                val response = adFetcher.fetchDisplayAdsWithAu(
                    cliUbid = "Any",
                    pageType = "demo_page",
                    productCount = 1,
                    adUnits = listOf("banner_ads"),
                    errorCallback = null
                )

                if (response == null) {
                    showError("Ad not available")
                    return@launch
                }

                Log.d("OSMOS_RESPONSE", response.toString())

                val responseMap = response["response"] as? Map<*, *>
                if (responseMap == null) {
                    showError("Invalid response")
                    return@launch
                }

                val dataJson = responseMap["data"]?.toString()
                if (dataJson.isNullOrEmpty()) {
                    showError("Ad not available")
                    return@launch
                }
                val rootObject = JSONObject(dataJson)

                val bannerArray = rootObject.getJSONObject("ads").getJSONArray("banner_ads")

                if (bannerArray.length() == 0) {
                    showError("Ad not available")
                    return@launch
                }
                val bannerObject = bannerArray.getJSONObject(0)
                val elements = bannerObject.getJSONObject("elements")
                imageUrl = elements.optString("value")
                clickUrl = bannerObject.optString("click_tracking_url")
                uclid = bannerObject.optString("uclid")
                bannerWidth = elements.optInt("width")
                bannerHeight = elements.optInt("height")
                if (imageUrl.isNullOrEmpty()) {
                    showError("Ad not available")
                    return@launch
                }
                binding.progressBar.visibility = View.GONE
                isLoading = false
                binding.btnLoadAd.isEnabled = true
                binding.ivBanner.visibility = View.VISIBLE
                Glide.with(this@MainActivity).load(imageUrl).into(binding.ivBanner)
                binding.tvStatus.text = "Status: Ad Loaded"
                setupBanner()

            } catch (e: Exception) {
                Log.e(
                    "OSMOS", "Fetch failed", e
                )
                showError("Failed to load ad")
            }
        }
    }

    private fun showError(message: String) {
        isLoading = false
        binding.progressBar.visibility = View.GONE
        binding.btnLoadAd.isEnabled = true
        binding.ivBanner.visibility = View.GONE
        binding.tvStatus.text = "Status: $message"
    }

    private fun setupBanner() {
        binding.ivBanner.post {
            if (bannerWidth > 0 && bannerHeight > 0) {
                val ratio = bannerHeight.toFloat() / bannerWidth
                val width = binding.ivBanner.width
                binding.ivBanner.layoutParams.height =
                    (width * ratio).toInt()
                binding.ivBanner.requestLayout()
            }

            binding.ivBanner.postDelayed({
                checkImpression()
            }, 300)
        }
        binding.ivBanner.setOnClickListener {
            if (uclid != null) {
                fireClick()
            }
            clickUrl?.let {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, it.toUri()
                    )
                )
            }
        }
    }

    private fun checkImpression() {
        if (impressionSent) return
        val rect = Rect()
        val visible = binding.ivBanner.getGlobalVisibleRect(rect)
        if (!visible) return
        val visibleArea = rect.width() * rect.height()
        val totalArea = binding.ivBanner.width * binding.ivBanner.height
        if (visibleArea >= totalArea * 0.5f) {
            impressionSent = true
            fireImpression()
        }
    }

    private fun fireImpression() {
        lifecycleScope.launch {
            try {
                uclid?.let {
                    registerEvent.registerAdImpressionEvent(
                            cliUbid = "Any",
                            uclid = it,
                            position = 1,
                            trackingParams = null,
                            errorCallback = null
                        )
                    Log.d("OSMOS", "Impression Fired")
                    binding.tvEvent.text = "Event: Impression Fired"
                }
            } catch (e: Exception) {
                Log.e(
                    "OSMOS", "Impression Failed", e
                )
            }
        }
    }

    private fun fireClick() {
        lifecycleScope.launch {
            try {
                uclid?.let {
                    registerEvent.registerAdClickEvent(
                            cliUbid = "Any", uclid = it, trackingParams = null, errorCallback = null
                        )
                    Log.d("OSMOS", "Click Fired")
                    binding.tvEvent.text = "Event: Click Fired"
                }
            } catch (e: Exception) {
                Log.e(
                    "OSMOS", "Click Failed", e
                )
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString("imageUrl", imageUrl)
        outState.putString("clickUrl", clickUrl)
        outState.putString("uclid", uclid)
        outState.putInt("bannerWidth", bannerWidth)
        outState.putInt("bannerHeight", bannerHeight)
        outState.putBoolean("impressionSent", impressionSent)
    }
}