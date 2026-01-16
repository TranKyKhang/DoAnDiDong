package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appmangxahoi.view.component.CreatePostTopBar
import com.example.appmangxahoi.view.component.AppBottomBar
import com.example.appmangxahoi.view.component.AppDrawer
import com.example.appmangxahoi.view.component.AppPostItem
import com.example.appmangxahoi.view.component.AppTopBar
import com.example.appmangxahoi.controller.Screen
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.ui.screens.CreatePostScreen
import com.example.appmangxahoi.view.component.ProfileTopBar
import kotlinx.coroutines.launch

@Composable
fun AppHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val postController = remember { post() }

    var posts by remember { mutableStateOf<List<PostModel>?>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val currentFeedTitle = if (currentRoute == "popular") "Popular" else "Home"

    var isPostButtonEnabled by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = postController.getFollowedPosts(context)

        if (result != null) {
            posts = result
        }

        isLoading = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onItemClick = { id ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate("community/$id")
                    }
                },
                onCreateGroupClick = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate("create_group")
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                val isSearchRoute = currentRoute?.startsWith("search") == true
                when {
                    currentRoute == Screen.Profile.route -> {
                        ProfileTopBar(
                            onBackClick = { navController.popBackStack() },
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    currentRoute == Screen.Create.route -> {
                        CreatePostTopBar(
                            onCloseClick = { navController.navigate(Screen.Home.route) },
                            onPostClick = { /* Xử lý đăng bài */ },
                            isPostEnabled = isPostButtonEnabled
                        )
                    }
                    // Ẩn TopBar ở các màn hình này
                    currentRoute == "settings" ||
                            currentRoute == "create_group" ||
                            currentRoute?.startsWith("community/") == true ||
                            isSearchRoute -> { }

                    // Màn Home/Popular/Inbox -> Hiện AppTopBar
                    else -> {
                        AppTopBar(
                            currentFeed = currentFeedTitle, // Truyền tiêu đề (Home/Popular)
                            onAvatarClick = { navController.navigate(Screen.Profile.route) },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSearchClick = { navController.navigate("search") },

                            // XỬ LÝ SỰ KIỆN CHỌN MENU (Home/Popular)
                            onFeedClick = { selected ->
                                if (selected == "Popular") {
                                    navController.navigate("popular") {
                                        // Giữ trạng thái Home, tránh reload lại từ đầu
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    // Quay về Home
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            },
            bottomBar = {
                val isSearchRoute = currentRoute?.startsWith("search") == true
                if (currentRoute != "settings" &&
                    currentRoute != "create_group" &&
                    !isSearchRoute &&
                    currentRoute != Screen.Create.route
                ) {
                    AppBottomBar(navController = navController)
                }
            }
        ) { innerPadding ->

            // --- NAVHOST ---
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(
                    // Chỉ lấy padding bottom, KHÔNG lấy padding top để tránh giật màn hình
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                // Màn Home
                composable(Screen.Home.route) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF0F0F0)),
                        // Tự đẩy nội dung xuống bằng chiều cao TopBar
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = 16.dp
                        )
                    ) {
                        items(posts!!.size) { item ->
                            AppPostItem(post = posts!![item])
                        }
                    }
                }

                // --- MÀN HÌNH POPULAR  ---
                composable("popular") {
                    PopularScreen(
                        topPadding = innerPadding.calculateTopPadding()
                    )
                }

                // Màn Create
                composable(Screen.Create.route) {
                    Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                        CreatePostScreen(
                            onContentChange = { isPostButtonEnabled = it }
                        )
                    }
                }

                // Màn Inbox
                composable(Screen.Inbox.route) {
                    InboxScreen(topPadding = innerPadding.calculateTopPadding())
                }

                // Màn Profile
                composable(Screen.Profile.route) { ProfileScreen( context = context)}

                // Màn Search
                composable(
                    route = "search?community={community}",
                    arguments = listOf(navArgument("community") { nullable = true })
                ) { backStackEntry ->
                    val communityArg = backStackEntry.arguments?.getString("community")
                    SearchScreen(
                        onBackClick = { navController.popBackStack() },
                        onPostClick = { },
                        targetCommunity = communityArg
                    )
                }

                // Các màn hình phụ khác
                composable("settings") {
                    SettingScreen(onBackClick = { navController.popBackStack() })
                }

                composable("create_group") {
                    CreateGroupScreen(
                        onDismiss = { navController.popBackStack() },
                        onCreate = { navController.popBackStack() }
                    )
                }

                composable("community/{id}", arguments = listOf(
                    navArgument("id") {
                        type = NavType.IntType
                    }
                )) { backStackEntry ->
                    val communityId = backStackEntry.arguments?.getInt("id") ?: return@composable
                    CommunityScreen(
                        communityId = communityId,
                        onBackClick = { navController.popBackStack() },
                        onSearchClick = { navController.navigate("search?community=$id") }
                    )
                }
            }
        }
    }
}