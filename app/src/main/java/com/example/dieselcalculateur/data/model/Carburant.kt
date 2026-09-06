package com.example.dieselcalculateur.data.model

enum class Carburant(val id: Int, val nom: String) {
    GAZOLE(1, "Gazole"),
    SP95(2, "SP95"),
    SP98(6, "SP98"),
    E10(5, "E10"),
    E85(3, "E85"),
    GPLC(4, "GPLc")
}
