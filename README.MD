<!-- HTML -->
<div class="video-thumb">
<style>
  .video-thumb {
    position: relative;
    display: inline-block;
    cursor: pointer;
    overflow: hidden;
    border-radius: 4px;
  }

  .video-thumb img {
    display: block;
    width: 100%;
    height: auto;
  }

  .video-thumb .watch-btn {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    padding: 0.5em 1em;
    border: none;
    border-radius: 2px;
    font-size: 1rem;
    text-transform: uppercase;
    pointer-events: none; /* let clicks pass through to the link */
  }
</style>
  <a href="https://www.youtube.com/watch?v=GhcdHBF9w9s" target="_blank" rel="noopener">
    <img src="https://img.youtube.com/vi/GhcdHBF9w9s/default.jpg" alt="Video Thumbnail">
    <div class="watch-btn">Watch</div>
  </a>
</div>



***Overview***

`SearchDropdownView` is a lightweight Android `View` that provides:
1. 🔍 Url based search (e.g. OpenStreetMap’s Nominatim)
2. 🕒 Built-in caching with configurable TTL
3. 🎨 Customizable styling for both the search box and dropdown items
4. 🔄 Callback hooks for success, error, and item selection

---

***Usage***

1. **Add to your layout XML**
    ```xml
    <LinearLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:fitsSystemWindows="true"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.aviadkorakin.search_and_dropdown.SearchDropdownView
            android:id="@+id/searchDropdown"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"

            app:apiUrl="https://nominatim.openstreetmap.org/search?format=json&amp;limit=5&amp;q={query}"
            app:hintText="Search for a place…"
            app:minChars="3"
            app:cacheTtlSeconds="10"

            app:loadingDrawable="@drawable/ic_loading"
            app:maxRows="4"

            app:primaryColor="@color/colorPrimary"
            app:backgroundColor="@android:color/white"
            app:textColor="@android:color/black"
            app:searchBoxStrokeColor="@color/colorAccent"
            app:searchBoxStrokeWidth="2dp"

            app:dropdownDisplayField="display_name"
            app:dropdownItemTextColor="@android:color/black"
            app:dropdownItemBackgroundColor="@android:color/white"
            app:dropdownItemTextSize="14sp"
            app:dropdownItemStrokeColor="@color/colorDivider"
            app:dropdownItemStrokeWidth="1dp"

            app:onSuccess="onSearchSuccess"
            app:onError="onSearchError"/>
    </LinearLayout>
    ```
   *Tip:* Include `{query}` in your `apiUrl` so it’s replaced with the user’s input. If omitted, the view will append `?q=…` or `&q=…` automatically.

2. **Hook up callbacks in your Activity/Fragment**
    ```kotlin
    class MainActivity : AppCompatActivity() {
        private lateinit var searchDropdown: SearchDropdownView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            searchDropdown = findViewById(R.id.searchDropdown)

            // Kotlin lambdas
            searchDropdown.setOnSuccessListener { results ->
                // results: List<Map<String, Any>>
            }
            searchDropdown.setOnErrorListener { error ->
                // error: Throwable
            }
            searchDropdown.setOnItemSelectedListener { item ->
                // item: Map<String, Any>>
            }
        }

        // DataBinding callbacks (if using XML onSuccess/onError)
        fun onSearchSuccess(results: List<Map<String, Any>>) { /*…*/ }
        fun onSearchError(error: Throwable) { /*…*/ }
    }
    ```

---

***XML Attributes***

| Icon | Attribute                         | Format           | Default                | Description                                                     |
|------|-----------------------------------|------------------|------------------------|-----------------------------------------------------------------|
| 🔗   | `app:apiUrl`                      | string           | —                      | Base URL for search endpoint. Supports `{query}` placeholder.  |
| 💡   | `app:hintText`                    | string           | none                   | Hint text in the search field.                                 |
| 🔢   | `app:minChars`                    | integer          | 1                      | Min characters before search.                                  |
| ⏳   | `app:cacheTtlSeconds`             | integer          | 5                      | TTL (seconds) for in-memory cache.                             |
| 🖼️   | `app:loadingDrawable`             | reference        | none                   | Drawable during loading (GIF or static).                       |
| 📊   | `app:maxRows`                     | integer          | 0 (unlimited)          | Max dropdown rows before scroll.                               |
| 🎨   | `app:primaryColor`                | color reference  | your theme primary     | Primary accent color.                                          |
| 🏞️   | `app:backgroundColor`             | color reference  | white                  | View background color.                                         |
| 🔤   | `app:textColor`                   | color reference  | black                  | Text color for input & items.                                  |
| 🔲   | `app:searchBoxStrokeColor`        | color reference  | dark gray              | Search box outline color.                                      |
| ➖   | `app:searchBoxStrokeWidth`        | dimension        | 1dp                    | Search box outline width.                                      |
| 📋   | `app:dropdownDisplayField`        | string           | `"display_name"`       | JSON field to display in items.                                |
| 🔤   | `app:dropdownItemTextColor`       | color reference  | same as `textColor`    | Text color for dropdown items.                                 |
| 🏷️   | `app:dropdownItemBackgroundColor` | color reference  | same as `backgroundColor` | Item background color.                                     |
| 🔠   | `app:dropdownItemTextSize`        | dimension        | 16sp                   | Font size for dropdown items.                                  |
| 🔲   | `app:dropdownItemStrokeColor`     | color reference  | same as `searchBoxStrokeColor` | Item outline color.                                    |
| ➖   | `app:dropdownItemStrokeWidth`     | dimension        | same as `searchBoxStrokeWidth` | Item outline width.                                    |
| ✅   | `app:onSuccess`                   | string           | none                   | Name of no-arg method to call on success.                      |
| ❌   | `app:onError`                     | string           | none                   | Name of single-Throwable-arg method to call on error.          |
