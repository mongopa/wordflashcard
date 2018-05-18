package xyz.mongop.wordfrashcard

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_word_list.*

class WordListActivity : AppCompatActivity(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    lateinit var realm:Realm

    lateinit var results: RealmResults<WordDB>

    lateinit var word_list: ArrayList<String>
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        //新しい単語の追加を押した場合
        buttonAddNewWord.setOnClickListener {
            val intent = Intent(this@WordListActivity, EditActivity::class.java)
            intent.putExtra(getString(R.string.intent_key_status), getString(R.string.status_add))
            startActivity(intent)
        }
        //戻るボタンを押した場合
        //メインへ
        buttonBack.setOnClickListener {
            finish()
        }

        //sortBtn押した場合
        buttonSort.setOnClickListener {
            results = realm.where<WordDB>(WordDB::class.java).findAll().sort(getString(R.string.db_field_memory_flag))

            //一旦表示しているword_listをクリア(そうしないと下に重なってしまうため)
            word_list.clear()

            results.forEach {
                if (it.boolMemoryFlag){
                    word_list.add(it.strQuestion + ": " + it.strAnswer + "【暗記済】")
                }else {
                    word_list.add(it.strQuestion + ": " + it.strAnswer)
                }
            }

            listView.adapter = adapter
        }

        //リストのクリックリスナー
        listView.onItemClickListener= this
        listView.setOnItemLongClickListener(this)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        //DBからタップした項目を取得
        val selectedDB = results[p2]
        val strSelectedQuestion = selectedDB.strQuestion
        val strSelectedAnswer = selectedDB.strAnswer

        //Editアクティビティを開く
        //取得した問題、答え、行番号とステータスをインテントで渡す
        val intent = Intent(this@WordListActivity,EditActivity::class.java)
        intent.putExtra(getString(R.string.intent_key_question), strSelectedQuestion)
        intent.putExtra(getString(R.string.intent_key_answer), strSelectedAnswer)
        intent.putExtra(getString(R.string.intent_key_position), p2)
        intent.putExtra(getString(R.string.intent_key_status), getString(R.string.status_change))
        startActivity(intent)
    }

    override fun onItemLongClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long): Boolean {
        //長押しした項目をDBから削除
        val selectedDB = results[p2]

        val dialog = AlertDialog.Builder(this@WordListActivity).apply {
            setTitle(selectedDB.strAnswer+"の削除")
            setMessage("削除してもよろしいでしょうか？")
            setPositiveButton("はい"){  dialog, which ->
                //取得した内容をDBから削除
                realm.beginTransaction()
                selectedDB.deleteFromRealm()
                realm.commitTransaction()
                //取得した内容を一覧(リスト)からも削除
                word_list.removeAt(p2)
                //DBからデータを再取得して表示
                listView.adapter = adapter
            }
            setNegativeButton("いいえ"){dialog, which->}
            //表示させる
            show()
        }

        return true
    }


    override fun onResume() {
        super.onResume()
        //Realmインスタンスの取得
        realm = Realm.getDefaultInstance()

        //DBに登録している単語一覧を表示(onCreateの方がonResumeより先に来るから、取得できない。)
        results = realm.where(WordDB::class.java).findAll().sort(getString(R.string.db_field_answer))

        word_list = ArrayList<String>()
        results.forEach {
            if(it.boolMemoryFlag){
                word_list.add(it.strQuestion + ": " + it.strAnswer +  "【暗記済】")
            }else{
                word_list.add(it.strQuestion + ": " + it.strAnswer)
            }

            }

        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, word_list)
        listView.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        //インスタンスの片付け
        realm.close()
    }
}
