package xyz.mongop.wordfrashcard

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.realm.Realm
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_edit.*

class EditActivity : AppCompatActivity() {

    lateinit var realm: Realm

    var strQuestion : String = ""
    var strAnswer: String = ""
    var intPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        //インテントの受け取り
        val bundle = intent.extras
        val strStatus = bundle.getString(getString(R.string.intent_key_status))
        textViewStatus.text = strStatus

        //修正の場合、問題答えの表示
        if (strStatus == getString(R.string.status_change)){
            strQuestion = bundle.getString(getString(R.string.intent_key_question))
            strAnswer = bundle.getString(getString(R.string.intent_key_answer))
            editTextQuestion.setText(strQuestion)
            editTextAnswer.setText(strAnswer)

            intPosition = bundle.getInt(getString(R.string.intent_key_position))

            //修正の場合、問題が修正できないようにする
            editTextQuestion.isEnabled = false

        }else{
            editTextQuestion.isEnabled = true
        }


        //登録ボタンを押した場合
        buttonRegister.setOnClickListener {
            if (strStatus == getString(R.string.status_add)) {
                //新しい単語の追加
                addNewWord()
            } else {
                //登録した単語の修正
                changeWord()
            }
        }
        //もどるボタンを押した場合
        buttonEditBack.setOnClickListener {
            finish()
        }
}
    private fun addNewWord() {
//新しい単語の追加

        if (editTextAnswer.text.toString() != "" && editTextQuestion.text.toString() != "") {
            //登録確認ダイアログ
            val dialog = AlertDialog.Builder(this@EditActivity).apply {
                setTitle("登録")
                setMessage("登録してもよろしいでしょうか？")
                setPositiveButton("はい") { dialog, which ->

                    //例外処理
                    //単語主キーの重複チェック
                    try {
                        realm.beginTransaction()   //開始処理

                        //主キー・暗記済みフラグ設定に伴う変更

                        //        val wordDB = realm.createObject(WordDB::class.java)　これは主キーを持たない場合の書き方
                        val wordDB = realm.createObject(WordDB::class.java, editTextQuestion.text.toString())
                        //        wordDB.strQuestion = editTextQuestion.text.toString()
                        wordDB.strAnswer = editTextAnswer.text.toString()
                        wordDB.boolMemoryFlag = false   //単語を登録時にはフラグを設定しない

                        //登録完了メッセージを表示
                        Toast.makeText(this@EditActivity, "登録が完了しました", Toast.LENGTH_SHORT).show()
                        finish()

                    } catch (e: RealmPrimaryKeyConstraintException) {
                        //メッセージを表示
                        Toast.makeText(this@EditActivity, "その単語はすでに登録されています。", Toast.LENGTH_SHORT).show()
                    } finally {
                        //入力した文字を消す
                        editTextQuestion.setText("")
                        editTextAnswer.setText("")

                        realm.commitTransaction()   //終了処理
                    }
                }
                setNegativeButton("いいえ") { dialog, which -> }
                show()
            }
        }else{
        Toast.makeText(this@EditActivity, "入力してください", Toast.LENGTH_SHORT).show()
        }
    }



    private fun changeWord() {
        //選択した行番号のレコードをDBから取得
        val results = realm.where(WordDB::class.java).findAll().sort(getString(R.string.db_field_question))
        val selectedDB = results.get(intPosition)   //=results[intPosition]

        //選択した行番号のれこーおをDBからから取得
        val dialog= AlertDialog.Builder(this@EditActivity).apply {
            setTitle(selectedDB.strAnswer + "の変更")
            setMessage("変更してもよろしいでしょうか？")
            setPositiveButton("はい"){dialog, which->
                //入力した問題・答えでレコードの更新
                realm.beginTransaction()
//        selectedDB.strQuestion = editTextQuestion.text.toString()

                selectedDB.strAnswer = editTextAnswer.text.toString()
                selectedDB.boolMemoryFlag = false  //単語を登録時にはフラグを設定しない
                realm.commitTransaction()

                //入力した文字を入力欄から消す
                editTextQuestion.setText("")
                editTextAnswer.setText("")

                //修正完了メッセージ
                Toast.makeText(this@EditActivity,"修正が完了しました", Toast.LENGTH_SHORT).show()
                //今の画面を閉じて単語一覧画面に戻る
                finish()
            }
            setNegativeButton("いいえ"){dialog, which ->}
            show()
        }
    }
    override fun onResume() {
        super.onResume()
        //Realmインスタンスの取得
        realm = Realm.getDefaultInstance()

    }

    override fun onPause() {
        super.onPause()
        //インスタンスの片付け
        realm.close()
    }
}