package xyz.mongop.wordfrashcard

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Owner on 2017/11/03.
 */
open class WordDB:RealmObject() {
    //問題
    @PrimaryKey
    open var strQuestion: String = ""

    //答え
    open var strAnswer:String = ""

    //暗記済みフラグ
    open var boolMemoryFlag: Boolean = false

}