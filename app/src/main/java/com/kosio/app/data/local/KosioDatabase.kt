package com.kosio.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kosio.app.data.local.dao.IngredientDao
import com.kosio.app.data.local.dao.ProductDao
import com.kosio.app.data.local.dao.SnapshotDao
import com.kosio.app.data.local.entity.CalculationSnapshotEntity
import com.kosio.app.data.local.entity.IngredientEntity
import com.kosio.app.data.local.entity.IngredientPriceEntity
import com.kosio.app.data.local.entity.ProductEntity
import com.kosio.app.data.local.entity.ProductIngredientEntity

@Database(
    entities = [
        IngredientEntity::class,
        IngredientPriceEntity::class,
        ProductEntity::class,
        ProductIngredientEntity::class,
        CalculationSnapshotEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class KosioDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun productDao(): ProductDao
    abstract fun snapshotDao(): SnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: KosioDatabase? = null

        fun getDatabase(context: Context): KosioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KosioDatabase::class.java,
                    "kosio_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
