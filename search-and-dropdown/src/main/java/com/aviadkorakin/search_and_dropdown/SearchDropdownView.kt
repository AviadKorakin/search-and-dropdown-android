package com.aviadkorakin.search_and_dropdown

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aviadkorakin.search_and_dropdown.databinding.ViewSearchDropdownBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class)
class SearchDropdownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val binding = ViewSearchDropdownBinding
       .inflate(LayoutInflater.from(context), this)

    //repository
    private var repo: SearchRepository

    // network & core
    private var apiUrl: String = ""
    private var minChars: Int = 1
    private var cacheTtlSeconds: Int = 5
    private var dropdownDisplayField: String = "display_name"  // default

    // visuals from attrs
    private var loadingDrawableRes: Int = 0
    private var maxRows: Int =0
    private var maxHeightPx = 0
    private var primaryColor: Int = 0
    private var backgroundColor: Int = 0
    private var textColor: Int = 0
    private var searchBoxStrokeColor: Int = 0
    private var searchBoxStrokeWidthPx: Int = 0

    // dropdown‐item attrs
    private var itemTextColor: Int = 0
    private var itemBackgroundColor: Int = 0
    private var itemTextSizePx: Float = 0f
    private var itemStrokeColor: Int = 0
    private var itemStrokeWidthPx: Int = 0

    private var hintText: String? = null

    // callbacks
    private var onSuccessMethod: String? = null
    private var onErrorMethod:   String? = null
    private var onItemSelectedListener: ((Map<String, Any>) -> Unit)? = null
    private var successListener: ((List<Map<String, Any>>) -> Unit)? = null
    private var errorListener:   ((Throwable) -> Unit)? = null

    //triggers
    private var suppressNextSearch = false
    var selectedItem: Map<String,Any>? = null
        private set


    init {
        orientation = VERTICAL

        context.theme.obtainStyledAttributes(
            attrs, R.styleable.SearchDropdownView, defStyle, 0
        ).apply {
            try {
                apiUrl    = getString(R.styleable.SearchDropdownView_apiUrl) ?: ""
                minChars  = getInteger(R.styleable.SearchDropdownView_minChars, 1)
                cacheTtlSeconds = getInteger(R.styleable.SearchDropdownView_cacheTtlSeconds, 5)
                loadingDrawableRes = getResourceId(
                    R.styleable.SearchDropdownView_loadingDrawable, 0
                )
                maxRows = getInt(R.styleable.SearchDropdownView_maxRows, 0)
                primaryColor      = getColor(
                    R.styleable.SearchDropdownView_primaryColor, primaryColor
                )
                backgroundColor   = getColor(
                    R.styleable.SearchDropdownView_backgroundColor, backgroundColor
                )
                textColor         = getColor(
                    R.styleable.SearchDropdownView_textColor, textColor
                )
                hintText          = getString(
                    R.styleable.SearchDropdownView_hintText
                )
                itemTextColor       = getColor(
                    R.styleable.SearchDropdownView_dropdownItemTextColor,
                    textColor
                )
                itemBackgroundColor = getColor(
                    R.styleable.SearchDropdownView_dropdownItemBackgroundColor,
                    backgroundColor
                )
                itemTextSizePx      = getDimension(
                    R.styleable.SearchDropdownView_dropdownItemTextSize,
                    resources.getDimension(R.dimen.dropdown_item_text_size)
                )
                searchBoxStrokeColor = getColor(
                    R.styleable.SearchDropdownView_searchBoxStrokeColor,
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
                searchBoxStrokeWidthPx = getDimensionPixelSize(
                    R.styleable.SearchDropdownView_searchBoxStrokeWidth,
                    resources.getDimensionPixelSize(R.dimen.default_search_box_stroke_width)
                )

                itemStrokeColor = getColor(
                    R.styleable.SearchDropdownView_dropdownItemStrokeColor,
                    searchBoxStrokeColor
                )
                itemStrokeWidthPx = getDimensionPixelSize(
                    R.styleable.SearchDropdownView_dropdownItemStrokeWidth,
                    searchBoxStrokeWidthPx
                )
                onSuccessMethod = getString(R.styleable.SearchDropdownView_onSuccess)
                onErrorMethod   = getString(R.styleable.SearchDropdownView_onError)
            } finally {
                recycle()
            }
        }

        repo = SearchRepository.create(apiUrl, cacheTtlSeconds)

        val gifView     = binding.loadingGifView
        val progressBar = binding.loadingProgressBar

        val useGif = runCatching {
            Glide.with(context)
                .asGif()
                .load(loadingDrawableRes)
                .submit()
                .get()
            true
        }.getOrDefault(false)

        if (useGif && loadingDrawableRes != 0) {
            progressBar.visibility = GONE
            gifView.visibility = VISIBLE
            Glide.with(context)
                .asGif()
                .load(loadingDrawableRes)
                .into(gifView)
        } else {
            gifView.visibility = GONE
            progressBar.visibility = VISIBLE
            progressBar.indeterminateDrawable =
                ContextCompat.getDrawable(context, loadingDrawableRes)
        }
        binding.searchFieldLayout.apply {
            boxStrokeColor = searchBoxStrokeColor
            setBoxStrokeWidth(searchBoxStrokeWidthPx/2)
        }

        binding.searchEditText.apply {
            setTextColor(textColor)
            hintText?.let { hint = it }
        }
        binding.resultsRecyclerView.apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                maxHeightPx
            )
            setBackgroundColor(backgroundColor)

        }

        binding.resultsRecyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = DropdownAdapter(
            onClick = { item ->

                suppressNextSearch = true

                selectedItem = item

                binding.searchEditText.setText(
                    item[dropdownDisplayField]?.toString().orEmpty()
                )


                onItemSelectedListener?.invoke(item)


                binding.resultsRecyclerView.visibility = GONE
            },
            displayField       = dropdownDisplayField,
            itemTextColor      = itemTextColor,
            itemBackgroundColor= itemBackgroundColor,
            itemFontSizePx     = itemTextSizePx,
            itemStrokeColor     = itemStrokeColor,
            itemStrokeWidthPx    = itemStrokeWidthPx
        )
        binding.resultsRecyclerView.adapter = adapter

        binding.resultsRecyclerView.post {
            if (maxRows > 0) {
                // now measuredWidth is valid
                val itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_dropdown, this, false)
                itemView.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )

                // Measure with exact width
                val widthSpec = MeasureSpec.makeMeasureSpec(
                    binding.resultsRecyclerView.width,
                    MeasureSpec.EXACTLY
                )
                val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                itemView.measure(widthSpec, heightSpec)

                val lp = itemView.layoutParams as? MarginLayoutParams
                val rowH = itemView.measuredHeight +
                        (lp?.topMargin ?: 0) + (lp?.bottomMargin ?: 0)
                maxHeightPx = rowH * maxRows

                // Apply it now that we have a real height
                binding.resultsRecyclerView.layoutParams =
                    binding.resultsRecyclerView.layoutParams.apply {
                        height = maxHeightPx
                    }
            }
        }


        binding.searchEditText.textChanges()
            .filter { !suppressNextSearch }
            .debounce(300)
            .filter { it.length >= minChars }
            .onEach { query ->
                fetchResults(query.toString(), adapter)
            }
            .launchIn(CoroutineScope(Dispatchers.Main))
    }

    private fun fetchResults(query: String, adapter: DropdownAdapter) {
              // show loading
             binding.loadingProgressBar.visibility = VISIBLE
            binding.loadingGifView.visibility = GONE

            // call into your SearchRepository
             CoroutineScope(Dispatchers.Main).launch {
                    try {
                        android.util.Log.d(
                                    "SearchDropdownView",
                                  "fetchResults() → query='$query', baseUrl='$apiUrl', minChars=$minChars, TTL=${cacheTtlSeconds}s"
                                         )
                        val results: List<Map<String, Any>> = repo.search(query)
                         binding.loadingProgressBar.visibility = GONE
                         adapter.submitList(results)
                         successListener?.invoke(results)
                         onSuccessMethod?.let { invokeCallback(it, results) }
                       } catch (t: Throwable) {
                        binding.loadingProgressBar.visibility = GONE
                        errorListener?.invoke(t)
                         onErrorMethod?.let { invokeCallback(it, t) }
                       }
                 }
           }

    private fun invokeCallback(methodName: String, param: Any) {
        runCatching {
            val m = context::class.java.getMethod(methodName, param::class.java)
            m.invoke(context, param)
        }.onFailure { it.printStackTrace() }
    }

    // Public setters
    fun setOnSuccessListener(listener: (List<Map<String, Any>>) -> Unit) {
        successListener = listener
    }
    fun setOnErrorListener(listener: (Throwable) -> Unit) {
        errorListener = listener
    }
    fun setOnItemSelectedListener(listener: (Map<String, Any>) -> Unit) {
        onItemSelectedListener = listener
    }

}