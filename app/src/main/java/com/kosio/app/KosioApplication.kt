package com.kosio.app

import android.app.Application
import com.kosio.app.data.backup.BackupManager
import com.kosio.app.data.local.KosioDatabase
import com.kosio.app.data.preferences.UserPreferencesRepository
import com.kosio.app.data.repository.IngredientRepositoryImpl
import com.kosio.app.data.repository.ProductRepositoryImpl
import com.kosio.app.data.repository.SnapshotRepositoryImpl
import com.kosio.app.domain.repository.IngredientRepository
import com.kosio.app.domain.repository.ProductRepository
import com.kosio.app.domain.repository.SnapshotRepository

class KosioApplication : Application() {

    lateinit var database: KosioDatabase
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var ingredientRepository: IngredientRepository
        private set

    lateinit var productRepository: ProductRepository
        private set

    lateinit var snapshotRepository: SnapshotRepository
        private set

    lateinit var backupManager: BackupManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = KosioDatabase.getDatabase(this)
        userPreferencesRepository = UserPreferencesRepository(this)
        ingredientRepository = IngredientRepositoryImpl(database)
        productRepository = ProductRepositoryImpl(database)
        snapshotRepository = SnapshotRepositoryImpl(database)
        backupManager = BackupManager(database)
    }

    companion object {
        lateinit var instance: KosioApplication
            private set
    }
}
