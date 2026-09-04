package com.example.dieselcalculateur.data.model

object CalculateurLogic {
    fun calculer(montantSouhaite: String, prixAffiche: String, prixPlafonne: String): CalculResult {
        val mSouhaiteClean = montantSouhaite.replace(",", ".")
        val pAfficheClean = prixAffiche.replace(",", ".")
        val pPlafonneClean = prixPlafonne.replace(",", ".")

        if (mSouhaiteClean.isBlank() || pAfficheClean.isBlank() || pPlafonneClean.isBlank()) {
            return CalculResult.Error("Veuillez remplir tous les champs.")
        }

        val mSouhaite = mSouhaiteClean.toDoubleOrNull()
        val pAffiche = pAfficheClean.toDoubleOrNull()
        val pPlafonne = pPlafonneClean.toDoubleOrNull()

        if (mSouhaite == null || pAffiche == null || pPlafonne == null) {
            return CalculResult.Error("Veuillez saisir un nombre valide.")
        }

        if (mSouhaite <= 0 || pAffiche <= 0) {
            return CalculResult.Error("Les valeurs doivent être supérieures à zéro.")
        }

        if (pPlafonne <= 0) {
            return CalculResult.Error("Le prix plafonné doit être supérieur à zéro.")
        }

        if (pPlafonne > pAffiche) {
            return CalculResult.Error("Le prix plafonné ne peut pas dépasser le prix affiché.")
        }

        val litres = mSouhaite / pPlafonne
        val montantAAnnoncer = litres * pAffiche
        val economie = montantAAnnoncer - mSouhaite

        return CalculResult.Success(
            litres = litres,
            montantAAnnoncer = montantAAnnoncer,
            economie = economie
        )
    }
}
