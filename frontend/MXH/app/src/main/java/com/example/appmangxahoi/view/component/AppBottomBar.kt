package com.example.appmangxahoi.view.component


import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appmangxahoi.controller.Screen

@Composable
fun AppBottomBar(navController: NavController) {
    // Danh sách các tab muốn hiện
    val items = listOf(
        Screen.Home,
        Screen.Create,
        Screen.Inbox
    )

    NavigationBar(
        //Chinh chieu cao cua topbar
        //modifier= Modifier.height(70.dp) ,
        containerColor = Color.White,

    ) {
        // Lấy thông tin xem mình đang đứng ở màn hình nào để tô đậm nút đó
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route


        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = { Icon(
                    //Neu dang chon dung icon dac, neu khong chon dung icon vien
                    imageVector = if(isSelected) screen.selectedIcon else screen.unselectedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                ) },
                label = {
                    Text(
                        text=screen.title,
                        fontSize = 10.sp,
                        lineHeight = 10.sp,// Chỉnh dòng cho khít
                        fontWeight = FontWeight.Medium // Đậm lên xíu cho dễ đọc vì chữ nhỏ
                    ) },
                // Nếu route hiện tại trùng với nút này -> chọn nó
                selected = currentRoute == screen.route,
                onClick = {
                    // Chuyển màn hình khi bấm
                    navController.navigate(screen.route) {
                        // Xóa các màn hình cũ khỏi stack để không bị đầy bộ nhớ
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}