package com.solid.number2048.game.entities

import androidx.compose.ui.graphics.Color
import com.solid.number2048.ui.theme.BoxColors


enum class BoxTypes(val number: Int, val color: Color, val label: String){

    NUM_2(2, BoxColors.PINK,  "2"),
    NUM_4(4, BoxColors.LIGHT_GREEN,  "4"),
    NUM_8(8, BoxColors.LIGHT_BLUE,  "8"),
    NUM_16(16, BoxColors.BLUE, "16"),
    NUM_32(32, BoxColors.SUNSET, "32"),
    NUM_64(64, BoxColors.LIGHT_PURPLE, "64"),
    NUM_128(128, BoxColors.ORANGE, "128"),
    NUM_256(256, BoxColors.VIOLET, "256"),
    NUM_512(512, BoxColors.DARK_GREEN, "512"),
    NUM_1024(1024, BoxColors.TEAL, "1K"),

}


val bronze = Color(255, 98, 0)
val platinum = Color(0xff939393)
val gold = Color(0xffecd02b)
val sapphire = Color(0xff3719f5)