package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.appmangxahoi.controller.Screen
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.ui.screens.CreatePostScreen
import com.example.appmangxahoi.view.component.*
import kotlinx.coroutines.launch

@Composable
fun AppHomeScreen() {
    val context = LocalContext.current
    val postController = remember { post() }

    // ✅ CHỈ 1 NavController
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ===== STATE =====
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshFeed by remember { mutableStateOf(0) } // reload trigger
    var isPostButtonEnabled by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentFeedTitle =
        if (currentRoute == "popular") "Popular" else "Home"

    // ===== LOAD POSTS =====
    LaunchedEffect(refreshFeed) {
        isLoading = true
        posts = postController.getFollowedPosts(context) ?: emptyList()
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
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
                            onCloseClick = {
                                navController.popBackStack()
                            },
                            onPostClick = { },
                            isPostEnabled = isPostButtonEnabled
                        )
                    }

                    currentRoute == "settings" ||
                            currentRoute == "create_group" ||
                            currentRoute?.startsWith("community/") == true ||
                            isSearchRoute -> {}

                    else -> {
                        AppTopBar(
                            currentFeed = currentFeedTitle,
                            onAvatarClick = { navController.navigate(Screen.Profile.route) },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSearchClick = { navController.navigate("search") },
                            onFeedClick = { selected ->
                                if (selected == "Popular") {
                                    navController.navigate("popular") {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }
            },
            bottomBar = {
                val isSearchRoute = currentRoute?.startsWith("search") == true
                if (
                    currentRoute != Screen.Create.route &&
                    currentRoute != "settings" &&
                    currentRoute != "create_group" &&
                    !isSearchRoute
                ) {
                    AppBottomBar(navController)
                }
            }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {

                // ===== HOME =====
                composable(Screen.Home.route) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF0F0F0)),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = 16.dp
                        )
                    ) {
                        items(posts) { post ->
                            AppPostItem(post = post)
                        }
                    }
                }

                // ===== POPULAR =====
                composable("popular") {
                    PopularScreen(
                        topPadding = innerPadding.calculateTopPadding()
                    )
                }

                // ===== CREATE POST =====
                composable(Screen.Create.route) {
                    Box(
                        modifier = Modifier.padding(
                            top = innerPadding.calculateTopPadding()
                        )
                    ) {
                        CreatePostScreen(
                            onContentChange = { isPostButtonEnabled = it },
                            onPostSuccess = {
                                refreshFeed++
                                navController.popBackStack(Screen.Home.route, false)

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Đăng bài thành công ",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            },
                            onPostFail = { errorMsg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        errorMsg ?: "Đăng bài thất bại ",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        )
                    }
                }

                // ===== INBOX =====
                composable(Screen.Inbox.route) {
                    InboxScreen(topPadding = innerPadding.calculateTopPadding())
                }

                // ===== PROFILE =====
                composable(Screen.Profile.route) {
                    ProfileScreen(context = context)
                }

                // ===== SEARCH =====
                composable(
                    route = "search?community={community}",
                    arguments = listOf(navArgument("community") { nullable = true })
                ) { backStackEntry ->
                    val communityArg = backStackEntry.arguments?.getString("community")
                    SearchScreen(
                        onBackClick = { navController.popBackStack() },
                        onPostClick = {},
                        targetCommunity = communityArg
                    )
                }

                // ===== SETTINGS =====
                composable("settings") {
                    SettingScreen(onBackClick = { navController.popBackStack() })
                }

                // ===== CREATE GROUP =====
                composable("create_group") {
                    CreateGroupScreen(
                        onDismiss = { navController.popBackStack() },
                        onCreate = { navController.popBackStack() }
                    )
                }

                // ===== COMMUNITY =====
                composable(
                    "community/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                    CommunityScreen(
                        communityId = id,
                        onBackClick = { navController.popBackStack() },
                        onSearchClick = {
                            navController.navigate("search?community=$id")
                        }
                    )
                }
            }
        }
    }
}
