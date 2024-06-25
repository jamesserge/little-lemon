package com.example.littlelemon

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.littlelemon.MenuNetwork.MenuItemNetwork
import com.example.littlelemon.ui.theme.LittleLemonTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var menuViewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences : SharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        menuViewModel = ViewModelProvider(this).get(MenuViewModel::class.java)

        setContent {
            LittleLemonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val MyNavigation = rememberNavController()
                        Navigation( navController = MyNavigation, sharedPreferences, menuViewModel )
                    }
                }
            }
        }
    }
}


class MenuViewModel(application: Application) : AndroidViewModel(application) {
    private val database by lazy {
        Room.databaseBuilder(application, AppDatabase::class.java, "database")
            .fallbackToDestructiveMigration() // Needed to update database version.
            .build()
    }

    val menuItems: LiveData<List<MenuItemRoom>> = database.menuItemDao().getAll()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (isMenuEmpty()) {
                val menuItemsNetwork = fetchMenu()
                saveMenuToDatabase(menuItemsNetwork)
            }
        }
    }

    private fun saveMenuToDatabase(menuItemsNetwork: List<MenuItemNetwork>) {
        val menuItemRoom = menuItemsNetwork.map { it.toMenuItemRoom() }
        viewModelScope.launch(Dispatchers.IO) {
            database.menuItemDao().insertAll(*menuItemRoom.toTypedArray())
        }
    }

    private suspend fun isMenuEmpty() : Boolean {
        return database.menuItemDao().isEmpty()
    }

    private suspend fun fetchMenu() : List<MenuItemNetwork> {
        val httpClient = HttpClient(Android) {
            install(ContentNegotiation) {
                json(contentType = ContentType("text", "plain"))
            }
        }
        return httpClient.get("https://raw.githubusercontent.com/Meta-Mobile-Developer-PC/Working-With-Data-API/main/menu.json")
            .body<MenuNetwork>()
            .menu
    }

    fun getFilteredMenuItems(searchPhrase: String, category: String) : LiveData<List<MenuItemRoom>> {
        return database.menuItemDao().getFilteredMenuItems("%$searchPhrase%", category)
    }

    fun setInitialData(menuItems: List<MenuItemRoom>) {
        (this.menuItems as MutableLiveData).value = menuItems
    }
}


//FOR TESTING
//
//class MainActivity : ComponentActivity() {
//    private val httpClient = HttpClient(Android) {
//        install(ContentNegotiation) {
//            json(contentType = ContentType("text", "plain"))
//        }
//    }
//    private val database by lazy {
//        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database")
//            .fallbackToDestructiveMigration() // Use destructive migration
//            .build()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//
//        setContent {
//            LittleLemonTheme {
//                val databaseMenuItems by database.menuItemDao().getAll().observeAsState(emptyList())
//                var menuItems = databaseMenuItems
//
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Box(modifier = Modifier.padding(innerPadding)) {
//
//                        Text(text = "Testing")
//                        MenuItemsList(menuItems)
//                    }
//                }
//            }
//        }
//        lifecycleScope.launch(Dispatchers.IO) {
//            if (database.menuItemDao().isEmpty()) {
//                val menuItemsNetwork = fetchMenu()
//                saveMenuToDatabase(menuItemsNetwork)
//            }
//        }
//    }
//
//    private suspend fun fetchMenu(): List<MenuItemNetwork> {
//        return httpClient.get("https://raw.githubusercontent.com/Meta-Mobile-Developer-PC/Working-With-Data-API/main/menu.json")
//            .body<MenuNetwork>()
//            .menu
//    }
//
//    private fun saveMenuToDatabase(menuItemNetwork: List<MenuItemNetwork>) {
//        val menuItemRoom = menuItemNetwork.map { it.toMenuItemRoom() }
//        database.menuItemDao().insertAll(*menuItemRoom.toTypedArray())
//    }
//}
//
//@Composable
//private fun MenuItemsList(items: List<MenuItemRoom>) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxHeight()
//            .padding(top = 20.dp)
//    ) {
//        items(
//            items = items,
//            itemContent = { menuItem ->
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    Text(text = menuItem.title)
//                    Text(
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(5.dp),
//                        textAlign = TextAlign.Right,
//                        text = String.format("%.2f", menuItem.price.toFloat()) // Ensure the price is a float
//                    )
//                }
//            }
//        )
//    }
//}