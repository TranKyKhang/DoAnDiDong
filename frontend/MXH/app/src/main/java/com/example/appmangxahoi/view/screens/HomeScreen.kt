package com.example.appmangxahoi.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.appmangxahoi.controller.Screen
import com.example.appmangxahoi.controller.community
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.ui.screens.CreatePostScreen
import com.example.appmangxahoi.utils.TokenManager
import com.example.appmangxahoi.view.component.*
import kotlinx.coroutines.launch

@Composable
fun AppHomeScreen(rootNavController: NavHostController) {
    val context = LocalContext.current
    val postController = remember { post() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // ===== STATE (GIỮ NGUYÊN) =====
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshFeed by remember { mutableStateOf(0) }
    var isPostButtonEnabled by remember { mutableStateOf(false) }
    // ===== PAGINATION (GIỮ NGUYÊN) =====
    var page by remember { mutableStateOf(1) }
    var isLastPage by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val community = remember { community() }
    val currentFeedTitle =
        if (currentRoute == "popular") "Popular" else "Home"
    // ===== LOAD POSTS (CHỈ THÊM currentRoute) =====
    LaunchedEffect(refreshFeed, page, currentRoute) {
        if (currentRoute == Screen.Home.route) {
            isLoading = true
            val result = postController.getFollowedPosts(context, page)
            posts = result ?: emptyList()
            isLastPage = result?.size ?: 0 < 20
            isLoading = false
        }
    }
    // ===== LẮNG NGHE CREATE GROUP (GIỮ NGUYÊN) =====
    val currentEntry by navController.currentBackStackEntryAsState()
    val refreshNeeded = currentEntry?.savedStateHandle
        ?.getLiveData<Boolean>("refresh_communities")
        ?.observeAsState()
    LaunchedEffect(refreshNeeded?.value) {
        if (refreshNeeded?.value == true) {
            community.getMyCommunity(context)
            currentEntry?.savedStateHandle?.remove<Boolean>("refresh_communities")
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                navController = navController,
                onItemClick = { id ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate("community/$id")
                    }
                },
                onCreateGroupClick = {}
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            onCloseClick = { navController.popBackStack() },
                            onPostClick = {},
                            isPostEnabled = isPostButtonEnabled
                        )
                    }
                    currentRoute == "settings" ||
                            currentRoute == "create_group" ||
                            currentRoute?.startsWith("community/") == true ||
                            currentRoute == "change_password" ||
                            isSearchRoute -> {}
                    else -> {
                        AppTopBar(
                            currentFeed = currentFeedTitle,
                            onAvatarClick = { navController.navigate(Screen.Profile.route) },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSearchClick = { navController.navigate("search") },
                            onFeedClick = { selected ->
                                if (selected == "Popular") {
                                    navController.navigate("popular")
                                } else {
                                    navController.navigate(Screen.Home.route)
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
                    currentRoute != "change_password" &&
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
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val prevInteractionSource = remember { MutableInteractionSource() }
                                IconButton(
                                    onClick = { page-- },
                                    enabled = page > 1 && !isLoading,
                                    interactionSource = prevInteractionSource
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Trang trước"
                                    )
                                }
                                val nextInteractionSource = remember { MutableInteractionSource() }
                                IconButton(
                                    onClick = { page++ },
                                    enabled = !isLastPage && !isLoading,
                                    interactionSource = nextInteractionSource
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Trang sau"
                                    )
                                }
                            }
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
                                    snackbarHostState.showSnackbar("Đăng bài thành công")
                                }
                            },
                            onPostFail = { errorMsg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        errorMsg ?: "Đăng bài thất bại"
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
                    SettingScreen(
                        rootNavController = rootNavController,
                        onBackClick = { navController.popBackStack() },
                        onChangePasswordClick = { navController.navigate("change_password") },
                        onLogoutClick = {
                            TokenManager.clearToken(context)
                            rootNavController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã đăng xuất thành công")
                            }
                        }
                    )
                }
                // ===== CHANGE PASSWORD =====
                composable("change_password") {
                    ChangePasswordScreen(
                        rootNavController = rootNavController,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                // ===== CREATE GROUP =====
                composable("create_group") {
                    val scope = rememberCoroutineScope()
                    val controller = remember { community() }
                    CreateGroupScreen(
                        onDismiss = { navController.popBackStack() },
                        onCreate = { data ->
                            scope.launch {
                                    val success = controller.createCommunity(context, data)
                                    if (success) {
                                        navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("refresh_communities", true)
                                        navController.popBackStack()
                                        Toast.makeText(context, "Tạo cộng đồng thành công", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
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