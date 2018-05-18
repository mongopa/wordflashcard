package xyz.mongop.wordfrashcard

import android.app.Application
import io.realm.Realm

/**
 * Created by Owner on 2017/11/03.
 */
class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        //Realmの初期化
        Realm.init(this)
    }
}